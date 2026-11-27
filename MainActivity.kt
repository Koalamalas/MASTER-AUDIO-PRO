package com.masteraudiopro

import android.app.Activity
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import android.graphics.Color
import android.graphics.Typeface

class MainActivity : Activity() {
    private val dsp = DspEngine()
    private var player: MediaPlayer? = null
    private var selectedUri: Uri? = null
    private lateinit var status: TextView
    private lateinit var playButton: Button
    private val red = Color.rgb(235, 45, 55)
    private val bg = Color.rgb(13, 14, 18)
    private val panel = Color.rgb(27, 29, 36)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = bg
        window.navigationBarColor = bg
        buildUi()
    }

    private fun buildUi() {
        val scroll = ScrollView(this).apply { setBackgroundColor(bg) }
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(18, 22, 18, 30)
        }
        scroll.addView(root)
        setContentView(scroll)

        root.addView(label("MASTER AUDIO PRO", 25, red, true))
        root.addView(label("32-BAND EQUALIZER  •  MASTERING", 12, Color.LTGRAY))
        root.addView(space(16))

        val mode = TextView(this).apply {
            text = "MODE: PLAYER INTERNAL  |  GLOBAL CAPTURE: TAHAP BERIKUTNYA"
            setTextColor(Color.WHITE); textSize = 12f
            setPadding(12, 14, 12, 14); setBackgroundColor(panel)
        }
        root.addView(mode)
        root.addView(space(12))

        val playerPanel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; setPadding(12, 12, 12, 12)
            setBackgroundColor(panel)
        }
        playerPanel.addView(label("MUSIC PLAYER", 16, Color.WHITE, true))
        status = label("Belum ada file musik dipilih", 12, Color.LTGRAY)
        playerPanel.addView(status)
        val controls = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        val open = button("PILIH MUSIK") { openAudio() }
        playButton = button("PLAY") { togglePlayback() }
        controls.addView(open, LinearLayout.LayoutParams(0, 48, 1f))
        controls.addView(playButton, LinearLayout.LayoutParams(0, 48, 1f).apply { leftMargin = 8 })
        playerPanel.addView(controls)
        root.addView(playerPanel)
        root.addView(space(14))

        root.addView(label("PRESET", 16, Color.WHITE, true))
        val presets = Spinner(this).apply {
            adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item,
                listOf("Flat", "Dangdut / Koplo", "Vocal", "Bass Boost", "Pop", "Rock"))
            setSelection(0)
            onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
                override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                    dsp.setPreset(parent?.getItemAtPosition(position).toString())
                }
            }
        }
        root.addView(presets)
        root.addView(space(12))

        root.addView(label("32-BAND GRAPHIC EQUALIZER", 17, Color.WHITE, true))
        root.addView(label("Rentang tiap band: -12 dB sampai +12 dB", 11, Color.LTGRAY))
        val names = dsp.frequencies.map { if (it >= 1000) "${it / 1000.0}k" else "${it.toInt()}" }
        for (i in 0 until 32) {
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL
                setPadding(6, 2, 6, 2)
            }
            val name = TextView(this).apply {
                text = names[i]; textSize = 11f; setTextColor(Color.LTGRAY)
                width = 48
            }
            val seek = SeekBar(this).apply {
                max = 240; progress = 120
                progressTintList = android.content.res.ColorStateList.valueOf(red)
                thumbTintList = android.content.res.ColorStateList.valueOf(red)
                setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(sb: SeekBar?, value: Int, fromUser: Boolean) {
                        dsp.setBandGain(i, (value - 120) / 10f)
                    }
                    override fun onStartTrackingTouch(sb: SeekBar?) {}
                    override fun onStopTrackingTouch(sb: SeekBar?) {}
                })
            }
            val value = TextView(this).apply {
                text = "0.0"; textSize = 10f; setTextColor(Color.WHITE); width = 38
            }
            seek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(sb: SeekBar?, p: Int, fromUser: Boolean) {
                    val db = (p - 120) / 10f
                    dsp.setBandGain(i, db); value.text = String.format("%.1f", db)
                }
                override fun onStartTrackingTouch(sb: SeekBar?) {}
                override fun onStopTrackingTouch(sb: SeekBar?) {}
            })
            row.addView(name); row.addView(seek, LinearLayout.LayoutParams(0, 38, 1f)); row.addView(value)
            root.addView(row)
        }

        root.addView(space(16))
        root.addView(label("DYNAMICS / MASTERING", 17, Color.WHITE, true))
        addValueControl(root, "Preamp", -12, 12, 0) { dsp.preampDb = it }
        addValueControl(root, "Compressor Threshold", -40, 0, -18) { dsp.thresholdDb = it }
        addValueControl(root, "Compressor Ratio", 1, 10, 3) { dsp.ratio = it }
        addValueControl(root, "Attack (ms)", 1, 100, 10) { dsp.attackMs = it }
        addValueControl(root, "Release (ms)", 20, 500, 120) { dsp.releaseMs = it }
        addValueControl(root, "Limiter Ceiling (dB)", -6, 0, -1) { dsp.limiterCeilingDb = it }
        root.addView(space(14))
        root.addView(label("Catatan: UI + DSP core tahap awal. Integrasi DSP ke jalur player dan Global Audio Capture akan diselesaikan pada milestone berikutnya.", 11, Color.LTGRAY))
    }

    private fun addValueControl(root: LinearLayout, title: String, min: Int, max: Int, initial: Int, onValue: (Float) -> Unit) {
        val row = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(4, 6, 4, 6) }
        val text = label("$title: $initial", 12, Color.LTGRAY)
        val seek = SeekBar(this).apply {
            this.max = max - min; progress = initial - min
            progressTintList = android.content.res.ColorStateList.valueOf(red)
            thumbTintList = android.content.res.ColorStateList.valueOf(red)
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(sb: SeekBar?, p: Int, fromUser: Boolean) {
                    val v = p + min; text.text = "$title: $v"; onValue(v.toFloat())
                }
                override fun onStartTrackingTouch(sb: SeekBar?) {}
                override fun onStopTrackingTouch(sb: SeekBar?) {}
            })
        }
        row.addView(text); row.addView(seek); root.addView(row)
    }

    private fun openAudio() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE); type = "audio/*"
        }
        startActivityForResult(intent, 10)
    }

    @Deprecated("Deprecated in Android API but retained for broad device compatibility")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 10 && resultCode == RESULT_OK) {
            selectedUri = data?.data
            selectedUri?.let {
                contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                status.text = it.lastPathSegment ?: "File musik dipilih"
                player?.release()
                player = MediaPlayer().apply {
                    setDataSource(this@MainActivity, it)
                    setOnPreparedListener { mp -> status.text = "Siap diputar"; playButton.text = "PLAY" }
                    setOnCompletionListener { playButton.text = "PLAY" }
                    prepareAsync()
                }
                status.text = "Memuat musik..."
            }
        }
    }

    private fun togglePlayback() {
        val p = player
        if (p == null) { status.text = "Pilih file musik terlebih dahulu"; return }
        try {
            if (p.isPlaying) { p.pause(); playButton.text = "PLAY" }
            else { p.start(); playButton.text = "PAUSE"; status.text = "Sedang diputar" }
        } catch (_: IllegalStateException) { status.text = "Audio belum siap, tunggu sebentar" }
    }

    private fun label(textValue: String, size: Int, color: Int, bold: Boolean = false) =
        TextView(this).apply {
            text = textValue; textSize = size.toFloat(); setTextColor(color)
            if (bold) typeface = Typeface.DEFAULT_BOLD
            setPadding(2, 5, 2, 5)
        }
    private fun space(h: Int) = android.view.View(this).apply {
        layoutParams = ViewGroup.LayoutParams(1, h)
    }
    private fun button(textValue: String, action: () -> Unit) =
        Button(this).apply {
            text = textValue; setTextColor(Color.WHITE)
            backgroundTintList = android.content.res.ColorStateList.valueOf(red)
            setOnClickListener { action() }
        }

    override fun onDestroy() {
        player?.release(); player = null
        super.onDestroy()
    }
}
