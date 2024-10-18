package com.gammaplay.findmyphone.utils;

import com.gammaplay.findmyphone.R;
import com.musicg.api.DetectionApi;
import com.musicg.wave.WaveHeader;

import java.util.List;

class ClapApi(waveHeader: WaveHeader?) : DetectionApi(waveHeader) {
    private var minClapFrequency = 1000.0
    private var maxClapFrequency = 10000.0

    init {
        init() // Default initialization
    }

    override fun init() {
        // Set default parameters for clap detection
        this.minFrequency = 1000.0
        this.maxFrequency = Double.MAX_VALUE
        this.minIntensity = 10000.0
        this.maxIntensity = 100000.0
        this.minStandardDeviation = 0.0
        this.maxStandardDeviation = 0.05000000074505806
        this.highPass = 100
        this.lowPass = 10000
        this.minNumZeroCross = 100
        this.maxNumZeroCross = 500
        this.numRobust = 4
    }

    // Adjust detection based on sensitivity levels
    fun setSensitivity(sensitivityLevelId: Int) {
        when (sensitivityLevelId) {
            R.string.sensitivity_low -> {
                // Raise minIntensity and narrow frequency range for softer sounds
                this.minIntensity = 25000.0
                this.maxIntensity = 50000.0
                this.minFrequency = 1500.0
                this.maxFrequency = 8000.0
                this.minClapFrequency = 1500.0
                this.maxClapFrequency = 8000.0
            }
            R.string.sensitivity_medium -> {
                this.minIntensity = 20000.0
                this.maxIntensity = 70000.0
                this.minFrequency = 1200.0
                this.maxFrequency = 9000.0
                this.minClapFrequency = 1200.0
                this.maxClapFrequency = 7000.0
            }
            R.string.sensitivity_high -> {
                this.minIntensity = 15000.0
                this.maxIntensity = 90000.0
                this.minFrequency = 1000.0
                this.maxFrequency = 8000.0
                this.minClapFrequency = 1000.0
                this.maxClapFrequency = 6000.0
            }
            R.string.sensitivity_maximum -> {
                // Maintain default parameters for maximum sensitivity
                this.minIntensity = 10000.0
                this.maxIntensity = 100000.0
                this.minFrequency = 1000.0
                this.maxFrequency = 5000.0
                this.minClapFrequency = 1000.0
                this.maxClapFrequency = 5000.0
            }
            else -> {
                // Default to medium sensitivity if unknown
                this.minIntensity = 15000.0
                this.maxIntensity = 70000.0
                this.minFrequency = 1200.0
                this.maxFrequency = 9000.0
                this.minClapFrequency = 1200.0
                this.maxClapFrequency = 7000.0
            }
        }
    }

    fun isClap(audioBytes: ByteArray): Boolean {
        // Check if the audio data matches the clap characteristics
        if (isSpecificSound(audioBytes) &&
            isWithinFrequencyRange(audioBytes, minClapFrequency, maxClapFrequency)) {
            return true;
        } else {
            return false;
        }
    }

    private fun isWithinFrequencyRange(audioBytes: ByteArray, minFrequency: Double, maxFrequency: Double): Boolean {
        // Implement a more accurate frequency analysis using FFT or other spectral analysis techniques
        val fft = FFT(audioBytes.size)
        fft.forwardTransform(audioBytes)

        val magnitudes = fft.getMagnitudes()
        val frequencies = fft.getFrequencies()

        for (i in magnitudes.indices) {
            if (magnitudes[i] > 0 && frequencies[i] >= minFrequency && frequencies[i] <= maxFrequency) {
                return true
            }
        }

        return false
    }

    private class FFT(n: Int) {
        private val n: Int = n.coerceIn(1, 2048)
        private val n2: Int = n shr 1
        private val logN: Int = 31 - Integer.numberOfLeadingZeros(n)
        private val omega: Array<Complex> = Array(n) { Complex(0.0, 0.0) }
        private val reverse: IntArray = IntArray(n)

        init {
            var i = 0
            var j = 0
            while (i < n) {
                reverse[i] = j
                j = j shr 1
                if (j >= n2) {
                    j = j xor n2
                }
                i++
            }

            for (i in 0 until n2) {
                val angle = -2 * Math.PI * i / n
                omega[i] = Complex(Math.cos(angle), Math.sin(angle))
            }
        }

        fun forwardTransform(data: ByteArray) {
            val complexData = Array(n) { Complex(0.0, 0.0) }
            for (i in 0 until n) {
                complexData[i] = Complex(data[i].toDouble() / 127.0, 0.0)
            }

            for (i in 0 until n) {
                complexData[reverse[i]] = complexData[i]
            }

            for (step in 1 until logN) {
                val stepSize = 1 shl step
                val halfStepSize = stepSize shr 1
                for (group in 0 until n step stepSize) {
                    for (i in 0 until halfStepSize) {
                        val omegaI = omega[i shl (logN - step)]
                        val t = omegaI * complexData[group + i + halfStepSize]
                        complexData[group + i + halfStepSize] = complexData[group + i] - t
                        complexData[group + i] = complexData[group + i] + t
                    }
                }
            }
        }

        fun getMagnitudes(): DoubleArray {
            val magnitudes = DoubleArray(n)
            for (i in 0 until n) {
                magnitudes[i] = Math.sqrt(omega[i].re * omega[i].re + omega[i].im * omega[i].im)
            }
            return magnitudes
        }

        fun getFrequencies(): DoubleArray {
            val sampleRate = 44100.0 // Adjust sample rate as needed
            val frequencies = DoubleArray(n)
            for (i in 0 until n) {
                frequencies[i] = i * sampleRate / n
            }
            return frequencies
        }
    }

    data class Complex(val re: Double, val im: Double) {
        operator fun plus(other: Complex): Complex {
            return Complex(re + other.re, im + other.im)
        }

        operator fun minus(other: Complex): Complex {
            return Complex(re - other.re, im - other.im)
        }

        operator fun times(other: Complex): Complex {
            return Complex(re * other.re - im * other.im, re * other.im + im * other.re)
        }
    }
}