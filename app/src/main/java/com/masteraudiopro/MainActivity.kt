
package com.masteraudiopro

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.widget.*
import android.view.ViewGroup

class MainActivity : Activity() {

    private var masterOn = false
    private lateinit var powerButton: Button
    private lateinit var status: TextView

    private val red = Color.rgb(235, 40, 50)
    private val dark = Color.rgb(15, 16, 20)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = dark
        window.navigationBarColor = dark

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setPadding(20, 24, 20, 20)
        root.setBackgroundColor(dark)

        val scroll = ScrollView(this)
        val panel = LinearLayout(this)
        panel.orientation = LinearLayout.VERTICAL

        val title = TextView(this)
        title.text = "MASTER AUDIO PRO"
        title.textSize = 25f
        title.setTextColor(Color.WHITE)
        title.typeface = Typeface.DEFAULT_BOLD
        panel.addView(title)

        val subtitle = TextView(this)
        subtitle.text = "PROFESSIONAL DIGITAL AUDIO PROCESSOR"
        subtitle.textSize = 10f
        subtitle.setTextColor(red)
        panel.addView(subtitle)

        status = TextView(this)
        status.text = "SYSTEM STANDBY"
        status.setTextColor(Color.LTGRAY)
        status.setPadding(0, 24, 0, 12)
        panel.addView(status)

        powerButton = Button(this)
        powerButton.text = "MASTER POWER : OFF"
        powerButton.setTextColor(Color.WHITE)
        powerButton.setBackgroundColor(Color.DKGRAY)
        powerButton.setOnClickListener {
            masterOn = !masterOn
            updatePower()
        }
        panel.addView(powerButton)

        addSection(panel, "32 BAND GRAPHIC EQUALIZER")

        val frequencies = arrayOf(
            "31","40","50","63","80","100","125","160",
            "200","250","315","400","500","630","800","1K",
            "1.25K","1.6K","2K","2.5K","3.15K","4K","5K",
            "6.3K","8K","10K","12.5K","14K","16K","18K","20K","AIR"
        )

        for (frequency in frequencies) {
            val row = LinearLayout(this)
            row.gravity = Gravity.CENTER_VERTICAL

            val label = TextView(this)
            label.text = frequency
            label.setTextColor(Color.WHITE)
            label.textSize = 11f
            row.addView(label, LinearLayout.LayoutParams(65, 48))

            val slider = SeekBar(this)
            slider.max = 24
            slider.progress = 12
            slider.progressTintList =
                android.content.res.ColorStateList.valueOf(red)
            row.addView(slider, LinearLayout.LayoutParams(0, 48, 1f))

            panel.addView(row)
        }

        addSection(panel, "DYNAMIC PROCESSING")
        addSection(panel, "COMPRESSOR")
        addSection(panel, "LIMITER")
        addSection(panel, "MASTER OUTPUT")

        val info = TextView(this)
        info.text = "MASTER AUDIO PRO v0.2"
        info.setTextColor(Color.GRAY)
        info.setPadding(0, 25, 0, 20)
        panel.addView(info)

        scroll.addView(panel)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)
    }

    private fun updatePower() {
        if (masterOn) {
            powerButton.text = "MASTER POWER : ON"
            powerButton.setBackgroundColor(red)
            status.text = "PROCESSOR ENABLED"
        } else {
            powerButton.text = "MASTER POWER : OFF"
            powerButton.setBackgroundColor(Color.DKGRAY)
            status.text = "SYSTEM STANDBY"
        }
    }

    private fun addSection(parent: LinearLayout, text: String) {
        val heading = TextView(this)
        heading.text = text
        heading.textSize = 15f
        heading.typeface = Typeface.DEFAULT_BOLD
        heading.setTextColor(red)
        heading.setPadding(0, 22, 0, 12)
        parent.addView(heading)
    }
}
