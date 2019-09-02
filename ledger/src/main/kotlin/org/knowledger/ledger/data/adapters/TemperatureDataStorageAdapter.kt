package org.knowledger.ledger.data.adapters

import org.knowledger.ledger.core.data.LedgerData
import org.knowledger.ledger.core.database.NewInstanceSession
import org.knowledger.ledger.core.database.StorageElement
import org.knowledger.ledger.core.database.StorageType
import org.knowledger.ledger.core.results.Outcome
import org.knowledger.ledger.core.storage.adapters.AbstractStorageAdapter
import org.knowledger.ledger.core.storage.results.DataFailure
import org.knowledger.ledger.crypto.hash.AvailableHashAlgorithms
import org.knowledger.ledger.data.TemperatureData
import org.knowledger.ledger.data.TemperatureUnit
import org.knowledger.ledger.results.tryOrDataUnknownFailure

object TemperatureDataStorageAdapter : AbstractStorageAdapter<TemperatureData>(
    TemperatureData::class.java,
    AvailableHashAlgorithms.SHA3512Hasher
) {
    override val properties: Map<String, StorageType>
        get() = mapOf(
            "temperature" to StorageType.DECIMAL,
            "unit" to StorageType.INTEGER
        )

    override fun store(
        toStore: LedgerData, session: NewInstanceSession
    ): StorageElement =
        (toStore as TemperatureData).let {
            session
                .newInstance(id)
                .setStorageProperty(
                    "temperature",
                    it.temperature
                ).setStorageProperty(
                    "unit",
                    when (it.unit) {
                        TemperatureUnit.Celsius -> TemperatureUnit.Celsius.ordinal
                        TemperatureUnit.Fahrenheit -> TemperatureUnit.Fahrenheit.ordinal
                        TemperatureUnit.Kelvin -> TemperatureUnit.Kelvin.ordinal
                        TemperatureUnit.Rankine -> TemperatureUnit.Rankine.ordinal
                    }
                )
        }


    override fun load(
        element: StorageElement
    ): Outcome<TemperatureData, DataFailure> =
        tryOrDataUnknownFailure {
            val prop = element.getStorageProperty<Int>("unit")
            val unit = when (prop) {
                TemperatureUnit.Celsius.ordinal -> TemperatureUnit.Celsius
                TemperatureUnit.Fahrenheit.ordinal -> TemperatureUnit.Fahrenheit
                TemperatureUnit.Kelvin.ordinal -> TemperatureUnit.Kelvin
                TemperatureUnit.Rankine.ordinal -> TemperatureUnit.Rankine
                else -> null
            }
            if (unit == null) {
                Outcome.Error<DataFailure>(
                    DataFailure.UnrecognizedUnit(
                        "Unit is not one of the expected: $prop"
                    )
                )
            } else {
                Outcome.Ok(
                    TemperatureData(
                        element.getStorageProperty("temperature"),
                        unit
                    )
                )
            }
        }

}