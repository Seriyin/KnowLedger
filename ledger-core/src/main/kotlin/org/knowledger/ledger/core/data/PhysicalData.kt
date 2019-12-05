@file:UseSerializers(InstantSerializer::class, GeoCoordinatesSerializer::class)
package org.knowledger.ledger.core.data

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.knowledger.ledger.core.base.data.DataCategory
import org.knowledger.ledger.core.base.data.GeoCoords
import org.knowledger.ledger.core.base.data.LedgerData
import org.knowledger.ledger.core.base.data.SelfInterval
import org.knowledger.ledger.core.base.serial.HashSerializable
import org.knowledger.ledger.core.base.storage.LedgerContract
import org.knowledger.ledger.core.serial.GeoCoordinatesSerializer
import org.knowledger.ledger.core.serial.InstantSerializer
import java.math.BigDecimal
import java.time.Instant

/**
 * Physical value is the main class in which to store ledger value.
 *
 * It requires an [instant] in which the value was recorded and
 * geo coordinates for where it was recorded.
 */
@Serializable
data class PhysicalData(
    val instant: Instant,
    val coords: GeoCoords,
    val data: LedgerData
) : HashSerializable,
    Cloneable,
    DataCategory by data,
    SelfInterval by data,
    Comparable<PhysicalData>,
    LedgerContract {
    public override fun clone(): PhysicalData =
        copy()

    override fun serialize(encoder: BinaryFormat): ByteArray =
        encoder.dump(serializer(), this)

    constructor(
        geoCoords: GeoCoords, data: LedgerData
    ) : this(Instant.now(), geoCoords, data)

    constructor(
        lat: BigDecimal, lng: BigDecimal,
        data: LedgerData
    ) : this(Instant.now(), GeoCoords(lat, lng), data)

    constructor(
        instant: Instant, lat: BigDecimal,
        lng: BigDecimal, data: LedgerData
    ) : this(instant, GeoCoords(lat, lng), data)

    constructor(
        instant: Instant, lat: BigDecimal,
        lng: BigDecimal, alt: BigDecimal,
        data: LedgerData
    ) : this(instant, GeoCoords(lat, lng, alt), data)

    override fun compareTo(other: PhysicalData): Int =
        when {
            instant > other.instant -> -1
            instant < other.instant -> 1
            else -> 0
        }
}