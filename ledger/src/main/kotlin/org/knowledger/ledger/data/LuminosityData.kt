@file:UseSerializers(BigDecimalSerializer::class)
package org.knowledger.ledger.data

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.knowledger.ledger.config.GlobalLedgerConfiguration.GLOBALCONTEXT
import org.knowledger.ledger.core.serial.BigDecimalSerializer
import java.io.InvalidClassException
import java.math.BigDecimal

/**
 * Luminosity value might be output by an ambient light
 * sensor, using lux units ([LuminosityUnit.Lux]) or a lighting unit,
 * outputting a specific amount of lumens ([LuminosityUnit.Lumens]),
 * according to [unit].
 */
@Serializable
@SerialName("LuminosityData")
data class LuminosityData(
    val luminosity: BigDecimal,
    val unit: LuminosityUnit
) : LedgerData {
    override fun clone(): LuminosityData =
        copy()

    override fun serialize(encoder: BinaryFormat): ByteArray =
        encoder.dump(serializer(), this)

    override fun calculateDiff(
        previous: SelfInterval
    ): BigDecimal =
        when (previous) {
            is LuminosityData -> calculateDiffLum(previous)
            else -> throw InvalidClassException(
                """SelfInterval supplied is:
                    |   ${previous.javaClass.name},
                    |   not ${this::class.java.name}
                """.trimMargin()
            )
        }

    private fun calculateDiffLum(
        previous: LuminosityData
    ): BigDecimal =
        luminosity.subtract(previous.luminosity)
            .divide(
                previous.luminosity,
                GLOBALCONTEXT
            )


    override fun toString(): String {
        return "LuminosityData(lum = $luminosity, unit = $unit)"
    }
}
