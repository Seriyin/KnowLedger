package org.knowledger.ledger.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.cbor.Cbor
import org.knowledger.ledger.core.data.LedgerData
import org.knowledger.ledger.core.data.SelfInterval
import java.io.InvalidClassException
import java.math.BigDecimal

@Serializable
@SerialName("PollutionAQData")
data class PollutionAQData(
    val lastUpdated: String,
    val unit: String,
    val parameter: PollutionType,
    val value: Double,
    val sourceName: String,
    val city: String = "TBD",
    val citySeqNum: Int = 1
) : LedgerData {
    val parameterDescription: String =
        parameter.description

    constructor(
        lastUpdated: String, unit: String,
        parameter: String, value: Double,
        sourceName: String
    ) : this(
        lastUpdated, unit,
        when (parameter) {
            "pm25" -> PollutionType.PM25
            "pm10" -> PollutionType.PM10
            "co" -> PollutionType.CO
            "bc" -> PollutionType.BC
            "so2" -> PollutionType.SO2
            "no2" -> PollutionType.NO2
            "o3" -> PollutionType.O3
            else -> PollutionType.NA
        }, value, sourceName
    )


    constructor(
        lastUpdated: String, unit: String,
        parameter: String, value: Double,
        sourceName: String, city: String,
        citySeqNum: Int
    ) : this(
        lastUpdated, unit,
        when (parameter) {
            "pm25" -> PollutionType.PM25
            "pm10" -> PollutionType.PM10
            "co" -> PollutionType.CO
            "bc" -> PollutionType.BC
            "so2" -> PollutionType.SO2
            "no2" -> PollutionType.NO2
            "o3" -> PollutionType.O3
            else -> PollutionType.NA
        }, value, sourceName,
        city, citySeqNum
    )

    override fun serialize(cbor: Cbor): ByteArray =
        cbor.dump(serializer(), this)

    override fun calculateDiff(
        previous: SelfInterval
    ): BigDecimal {
        return if (previous is PollutionAQData) {
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
        previous: PollutionAQData
    ): BigDecimal {
        TODO()
    }

}
