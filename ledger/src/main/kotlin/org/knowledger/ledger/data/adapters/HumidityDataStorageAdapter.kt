package org.knowledger.ledger.data.adapters

import kotlinx.serialization.KSerializer
import org.knowledger.ledger.core.adapters.AbstractStorageAdapter
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.data.HumidityData
import org.knowledger.ledger.data.HumidityUnit
import org.knowledger.ledger.database.NewInstanceSession
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.StorageType
import org.knowledger.ledger.database.results.DataFailure
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.err
import org.knowledger.ledger.results.ok
import org.knowledger.ledger.storage.LedgerData
import java.math.BigDecimal

class HumidityDataStorageAdapter(hashers: Hashers) :
    AbstractStorageAdapter<HumidityData>(HumidityData::class, hashers) {
    override val serializer: KSerializer<HumidityData> get() = HumidityData.serializer()

    override val properties: Map<String, StorageType>
        get() = mapOf("humidity" to StorageType.DECIMAL, "unit" to StorageType.INTEGER)

    override fun store(toStore: LedgerData, session: NewInstanceSession): StorageElement =
        (toStore as HumidityData).let { humidityData ->
            val unit = when (humidityData.unit) {
                HumidityUnit.GramsByKilograms -> HumidityUnit.GramsByKilograms.ordinal
                HumidityUnit.KilogramsByKilograms -> HumidityUnit.KilogramsByKilograms.ordinal
                HumidityUnit.Relative -> HumidityUnit.Relative.ordinal
            }
            session.newInstance(id)
                .setStorageProperty("humidity", humidityData.humidity)
                .setStorageProperty("unit", unit)
        }

    override fun load(element: StorageElement): Outcome<HumidityData, DataFailure> =
        commonLoad(element, id) {
            val prop = getStorageProperty<Int>("unit")
            val unit = when (prop) {
                HumidityUnit.GramsByKilograms.ordinal -> HumidityUnit.GramsByKilograms
                HumidityUnit.KilogramsByKilograms.ordinal -> HumidityUnit.KilogramsByKilograms
                HumidityUnit.Relative.ordinal -> HumidityUnit.Relative
                else -> null
            }
            val humidity = getStorageProperty<BigDecimal>("humidity")
            unit?.let { unit1 -> HumidityData(humidity, unit1).ok() }
            ?: DataFailure.UnrecognizedUnit("HUnit is not one of the expected: $prop").err()
        }

}