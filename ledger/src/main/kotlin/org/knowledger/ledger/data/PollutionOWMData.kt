package org.knowledger.ledger.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.cbor.Cbor
import org.knowledger.ledger.core.data.LedgerData
import org.knowledger.ledger.core.data.SelfInterval
import java.io.InvalidClassException
import java.math.BigDecimal


@Serializable
@SerialName("PollutionOWMData")
data class PollutionOWMData(
    val unit: String,
    var parameter: PollutionType,
    val value: Double,
    var data: List<List<Double>>,
    val city: String = "",
    val citySeqNum: Int = 1
) : LedgerData {
    val parameterDescription: String =
        parameter.description

    //mean of Value/Precision
    //Value
    //Precision
    val meanValuePrecision: Pair<Double, Double>

    init {
        var first = 0.0
        var second = 0.0
        var validElements = 0
        if (value == -99.0) {
            for (values in data) {
                if (!values[0].isNaN() && !values[1].isNaN()) {
                    first += values[0]
                    second += values[1]
                    validElements++
                }
            }
            first /= validElements
            second /= validElements
        }
        meanValuePrecision = first to second
    }


    constructor(
        unit: String,
        parameter: String,
        value: Double,
        data: List<List<Double>>,
        city: String = "",
        citySeqNum: Int = 1
    ) : this(
        unit,
        when (parameter) {
            "O3" -> PollutionType.O3
            "UV" -> PollutionType.UV
            "CO" -> PollutionType.CO
            "SO2" -> PollutionType.SO2
            "NO2" -> PollutionType.NO2
            else -> PollutionType.NA
        },
        when (parameter) {
            "O3", "UV" -> value
            else -> -99.0
        },
        when (parameter) {
            "CO", "SO2", "NO2" -> data.asSequence().map {
                it.asSequence().toList()
            }.toList()
            else -> emptyList()
        },
        city,
        citySeqNum
    )

    override fun serialize(cbor: Cbor): ByteArray =
        cbor.dump(serializer(), this)

    override fun calculateDiff(previous: SelfInterval): BigDecimal {
        return if (previous is PollutionOWMData) {
            calculateDiffPollution(previous)
        } else {
            throw InvalidClassException(
                """SelfInterval supplied is:
                    |   ${previous.javaClass.name},
                    |   not ${this::class.java.name}
                """.trimMargin()
            )
        }
    }

    private fun calculateDiffPollution(
        previous: PollutionOWMData
    ): BigDecimal {
        TODO()
    }

}
