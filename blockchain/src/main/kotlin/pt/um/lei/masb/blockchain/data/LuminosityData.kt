package pt.um.lei.masb.blockchain.data

import com.orientechnologies.orient.core.record.OElement
import com.orientechnologies.orient.core.record.impl.ODocument
import kotlinx.serialization.Serializable
import pt.um.lei.masb.blockchain.Coinbase
import java.io.InvalidClassException
import java.math.BigDecimal

/**
 * Luminosity data might be output by an ambient light sensor, using lux units
 * or a lighting unit, outputting a specific amount of lumens, according to [unit].
 */
@Serializable
data class LuminosityData(
    val lum: BigDecimal,
    val unit: LUnit
) : BlockChainData {
    override fun store(): OElement =
        ODocument("Luminosity").let {
            it.setProperty("lum", lum)
            it.setProperty(
                "unit", when (unit) {
                    LUnit.LUMENS -> 0x00.toByte()
                    LUnit.LUX -> 0x01.toByte()
                }
            )
            it
        }


    override fun calculateDiff(
        previous: SelfInterval
    ): BigDecimal =
        when (previous) {
            is LuminosityData -> calculateDiffLum(previous)
            else ->
                throw InvalidClassException(
                    "SelfInterval supplied is not ${this::class.simpleName}"
                )
        }

    private fun calculateDiffLum(previous: LuminosityData): BigDecimal =
        lum.subtract(previous.lum)
            .divide(previous.lum, Coinbase.MATH_CONTEXT)


    override fun toString(): String {
        return "LuminosityData(lum=$lum, unit=$unit)"
    }
}
