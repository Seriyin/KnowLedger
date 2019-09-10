package org.knowledger.example.slave

import org.knowledger.ledger.core.data.PhysicalData
import org.knowledger.ledger.data.NoiseData
import org.knowledger.ledger.data.NoiseUnit
import org.tinylog.kotlin.Logger
import java.math.BigDecimal
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.DataLine
import javax.sound.sampled.TargetDataLine
import kotlin.experimental.and
import kotlin.math.abs
import kotlin.math.sqrt

fun captureSound(): PhysicalData? {
    val line: TargetDataLine
    val bufSize = 2048

    //WAV format
    val format = AudioFormat(
        AudioFormat.Encoding.PCM_SIGNED,
        44100F,
        16,
        2,
        4,
        44100F,
        false
    )
    val info = DataLine.Info(TargetDataLine::class.java, format) // format is an AudioFormat object

    return if (!AudioSystem.isLineSupported(info)) {
        Logger.error { "Line not supported" }
        null
    } else {
        // Obtain and open the line.
        line = AudioSystem.getLine(info) as TargetDataLine
        line.open(format, bufSize)
        line.start()

        val buf = ByteArray(bufSize)
        val samples = DoubleArray(bufSize / 2)
        val b = line.read(buf, 0, buf.size)
        while (b > -1) {

            // convert bytes to samples here
            var i = 0
            var s = 0
            while (i < b) {
                var sample = 0

                sample = sample or ((buf[i++] and 0xFF.toByte()).toInt()) // (reverse these two lines
                sample = sample or (buf[i++].toInt() shl 8)   //  if the format is big endian)

                // normalize to range of +/-1.0f
                samples[s++] = sample / 32768.0
            }
        }

        var peak = 0.0
        var rms = 0.0

        for (sample in samples) {

            val abs = abs(sample)
            if (abs > peak) {
                peak = abs
            }

            rms += sample * sample
        }

        PhysicalData(
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            NoiseData(
                BigDecimal(sqrt(rms / samples.size)),
                BigDecimal(peak), NoiseUnit.Rms
            )
        )
    }
}