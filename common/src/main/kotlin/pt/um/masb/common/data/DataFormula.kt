package pt.um.masb.common.data

import java.math.BigDecimal
import java.math.MathContext

typealias DataFormula = (
    base: Int,
    timeBase: Int,
    deltaTime: BigDecimal,
    valueBase: Int,
    deltaValue: BigDecimal,
    constant: Int,
    threshold: Int,
    mathContext: MathContext
) -> BigDecimal


fun calculateDiff(
    base: Int,
    timeBase: Int,
    deltaTime: BigDecimal,
    valueBase: Int,
    deltaValue: BigDecimal,
    constant: Int,
    threshold: Int,
    mathContext: MathContext
): BigDecimal {
    val standardDivisor = BigDecimal(threshold * constant)
    val timeFactor = deltaTime
        .multiply(BigDecimal(timeBase))
        .pow(2, mathContext)
        .divide(standardDivisor, mathContext)

    val valueFactor = deltaValue
        .divide(BigDecimal(2), mathContext)
        .multiply(BigDecimal(valueBase))
        .divide(standardDivisor, mathContext)

    val baseFactor = BigDecimal(base).divide(standardDivisor, mathContext)
    return timeFactor
        .add(valueFactor)
        .add(baseFactor)
}

object DataDefaults {
    const val DEFAULT_VALUABLE: Int = 5
    const val DEFAULT_UNKNOWN: Int = 50
}