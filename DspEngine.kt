package com.masteraudiopro

import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * DSP core prototype: 32 cascaded peaking biquads + soft-knee compressor + limiter.
 * Input/output samples are normalized Float PCM in [-1, 1].
 */
class DspEngine(sampleRate: Float = 48000f) {
    val frequencies = doubleArrayOf(
        20.0, 22.0, 25.0, 31.5, 40.0, 50.0, 63.0, 80.0,
        100.0, 125.0, 160.0, 200.0, 250.0, 315.0, 400.0, 500.0,
        630.0, 800.0, 1000.0, 1250.0, 1600.0, 2000.0, 2500.0, 3150.0,
        4000.0, 5000.0, 6300.0, 8000.0, 10000.0, 12500.0, 16000.0, 20000.0
    )
    val gainsDb = FloatArray(32)
    var preampDb = 0f
    var thresholdDb = -18f
    var ratio = 3f
    var attackMs = 10f
    var releaseMs = 120f
    var limiterCeilingDb = -1f
    private var rate = sampleRate
    private val filters = Array(32) { Biquad() }
    private var envelope = 0f
    private var limiterGain = 1f

    fun setSampleRate(value: Float) {
        rate = value.coerceAtLeast(8000f)
        updateFilters()
    }

    fun setBandGain(index: Int, gain: Float) {
        if (index in 0..31) {
            gainsDb[index] = gain.coerceIn(-12f, 12f)
            updateFilter(index)
        }
    }

    fun setPreset(name: String) {
        gainsDb.fill(0f)
        when (name.lowercase()) {
            "bass boost" -> for (i in 0..7) gainsDb[i] = 4f
            "vocal" -> {
                for (i in 9..13) gainsDb[i] = -2f
                for (i in 17..24) gainsDb[i] = 2.5f
            }
            "dangdut / koplo" -> {
                for (i in 2..7) gainsDb[i] = 2f
                for (i in 10..13) gainsDb[i] = -1.5f
                for (i in 17..23) gainsDb[i] = 2f
                for (i in 25..29) gainsDb[i] = 1f
            }
            "pop" -> {
                for (i in 4..7) gainsDb[i] = 1.5f
                for (i in 17..22) gainsDb[i] = 1.5f
                for (i in 27..30) gainsDb[i] = 2f
            }
            "rock" -> {
                for (i in 2..6) gainsDb[i] = 3f
                for (i in 12..16) gainsDb[i] = -1.5f
                for (i in 22..28) gainsDb[i] = 2.5f
            }
        }
        updateFilters()
    }

    fun process(input: FloatArray, output: FloatArray, frames: Int, channels: Int) {
        val count = minOf(input.size, output.size, frames * channels)
        val attack = expCoefficient(attackMs)
        val release = expCoefficient(releaseMs)
        val ceiling = dbToLinear(limiterCeilingDb)
        val preamp = dbToLinear(preampDb)
        for (n in 0 until count) {
            var x = input[n] * preamp
            for (f in filters) x = f.process(x)
            val abs = kotlin.math.abs(x)
            val coeff = if (abs > envelope) attack else release
            envelope = coeff * envelope + (1f - coeff) * abs
            val envDb = linearToDb(envelope)
            var gainDb = 0f
            if (envDb > thresholdDb) {
                gainDb = (thresholdDb + (envDb - thresholdDb) / ratio) - envDb
            }
            x *= dbToLinear(gainDb)
            val peak = kotlin.math.abs(x)
            val targetGain = if (peak > ceiling) ceiling / peak else 1f
            limiterGain = minOf(targetGain, limiterGain + 0.0025f)
            output[n] = (x * limiterGain).coerceIn(-1f, 1f)
        }
    }

    private fun updateFilters() { for (i in filters.indices) updateFilter(i) }
    private fun updateFilter(i: Int) {
        filters[i].setPeaking(rate, frequencies[i].coerceAtMost(rate * 0.45), 1.15, gainsDb[i])
    }
    private fun expCoefficient(ms: Float): Float =
        kotlin.math.exp((-1.0 / (rate * (ms.coerceAtLeast(0.1f) / 1000.0))).toFloat())
    private fun dbToLinear(db: Float) = 10f.pow(db / 20f)
    private fun linearToDb(v: Float) = 20f * kotlin.math.log10(v.coerceAtLeast(1e-7f))

    private class Biquad {
        private var b0 = 1f; private var b1 = 0f; private var b2 = 0f
        private var a1 = 0f; private var a2 = 0f
        private var x1 = 0f; private var x2 = 0f
        private var y1 = 0f; private var y2 = 0f

        fun setPeaking(sr: Float, frequency: Double, q: Double, gainDb: Float) {
            val a = 10.0.pow(gainDb / 40.0)
            val w = 2.0 * PI * frequency / sr
            val alpha = kotlin.math.sin(w) / (2.0 * q)
            val c = kotlin.math.cos(w)
            val aa0 = 1.0 + alpha / a
            b0 = ((1.0 + alpha * a) / aa0).toFloat()
            b1 = ((-2.0 * c) / aa0).toFloat()
            b2 = ((1.0 - alpha * a) / aa0).toFloat()
            a1 = ((-2.0 * c) / aa0).toFloat()
            a2 = ((1.0 - alpha / a) / aa0).toFloat()
        }

        fun process(x: Float): Float {
            val y = b0*x + b1*x1 + b2*x2 - a1*y1 - a2*y2
            x2=x1; x1=x; y2=y1; y1=y
            return y
        }
    }
}
