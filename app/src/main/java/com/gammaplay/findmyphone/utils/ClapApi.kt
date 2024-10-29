package com.gammaplay.findmyphone.utils

import com.gammaplay.findmyphone.R
import com.musicg.api.DetectionApi
import com.musicg.wave.WaveHeader
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class ClapApi(waveHeader: WaveHeader?) : DetectionApi(waveHeader) {

    private var minClapFrequency = 1000.0
    private var maxClapFrequency = 10000.0

    init {
        init()
    }

    override fun init() {
        // Default clap detection parameters
        minFrequency = 1000.0
        maxFrequency = Double.MAX_VALUE
        minIntensity = 10000.0
        maxIntensity = 100000.0
        minStandardDeviation = 0.0
        maxStandardDeviation = 0.05
        highPass = 100
        lowPass = 10000
        minNumZeroCross = 100
        maxNumZeroCross = 500
        numRobust = 4
    }

    // Set sensitivity levels based on predefined parameters
    fun setSensitivity(sensitivityLevelId: Int) {
        val settings = when (sensitivityLevelId) {
            R.string.sensitivity_low -> SensitivitySettings(25000.0, 50000.0, 1500.0, 8000.0)
            R.string.sensitivity_medium -> SensitivitySettings(20000.0, 70000.0, 1200.0, 9000.0)
            R.string.sensitivity_high -> SensitivitySettings(15000.0, 90000.0, 1000.0, 8000.0)
            R.string.sensitivity_maximum -> SensitivitySettings(10000.0, 100000.0, 1000.0, 5000.0)
            else -> SensitivitySettings(15000.0, 70000.0, 1200.0, 9000.0)
        }

        minIntensity = settings.minIntensity
        maxIntensity = settings.maxIntensity
        minFrequency = settings.minFrequency
        maxFrequency = settings.maxFrequency
        minClapFrequency = settings.minFrequency
        maxClapFrequency = settings.maxFrequency
    }

    // Checks if audio data represents a clap
    fun isClap(audioBytes: ByteArray): Boolean {
        return isSpecificSound(audioBytes) && isWithinFrequencyRange(audioBytes)
    }

    private fun isWithinFrequencyRange(audioBytes: ByteArray): Boolean {
        val fft = FFT(audioBytes.size)
        fft.forwardTransform(audioBytes)
        val frequencies = fft.getFrequencies()
        return frequencies.any { it in minClapFrequency..maxClapFrequency }
    }

    // Settings for sensitivity levels
    private data class SensitivitySettings(
        val minIntensity: Double,
        val maxIntensity: Double,
        val minFrequency: Double,
        val maxFrequency: Double
    )

    // FFT implementation
    private class FFT(private val n: Int) {
        private val halfN = n / 2
        private val logN = 31 - Integer.numberOfLeadingZeros(n)
        private val omega = Array(n) { Complex(0.0, 0.0) }
        private val reverse = IntArray(n)

        init {
            for (i in 0 until n) {
                reverse[i] = i.reverseBits(logN)
            }
            for (i in 0 until halfN) {
                val angle = -2 * PI * i / n
                omega[i] = Complex(cos(angle), sin(angle))
            }
        }

        fun forwardTransform(data: ByteArray) {
            val complexData = Array(n) { i -> Complex(data.getOrNull(i)?.toDouble()?.div(127.0) ?: 0.0, 0.0) }
            bitReverseCopy(complexData)
            performFFT(complexData)
        }

        private fun bitReverseCopy(data: Array<Complex>) {
            for (i in 0 until n) {
                if (i < reverse[i]) {
                    val temp = data[i]
                    data[i] = data[reverse[i]]
                    data[reverse[i]] = temp
                }
            }
        }

        private fun performFFT(data: Array<Complex>) {
            for (s in 1..logN) {
                val m = 1 shl s
                val m2 = m / 2
                for (k in 0 until n step m) {
                    for (j in 0 until m2) {
                        val t = omega[j * (halfN / m2)] * data[k + j + m2]
                        data[k + j + m2] = data[k + j] - t
                        data[k + j] = data[k + j] + t
                    }
                }
            }
        }

        fun getFrequencies(): List<Double> {
            val sampleRate = 44100.0
            return List(n / 2) { it * sampleRate / n }
        }
    }

    // Complex number for FFT calculations
    private data class Complex(val re: Double, val im: Double) {
        operator fun plus(other: Complex) = Complex(re + other.re, im + other.im)
        operator fun minus(other: Complex) = Complex(re - other.re, im - other.im)
        operator fun times(other: Complex) = Complex(re * other.re - im * other.im, re * other.im + im * other.re)
    }
}

// Extension function for reversing bits (for FFT bit-reversal permutation)
private fun Int.reverseBits(bits: Int): Int {
    var x = this
    var y = 0
    repeat(bits) {
        y = (y shl 1) or (x and 1)
        x = x shr 1
    }
    return y
}
