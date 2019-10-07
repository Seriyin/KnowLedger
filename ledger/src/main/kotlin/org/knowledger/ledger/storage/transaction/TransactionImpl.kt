@file:UseSerializers(PublicKeySerializer::class)
package org.knowledger.ledger.storage.transaction

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.knowledger.ledger.core.serial.PublicKeySerializer
import org.knowledger.ledger.data.PhysicalData
import java.security.PublicKey

@Serializable
internal data class TransactionImpl(
    // Agent's pub key.
    override val publicKey: PublicKey,
    override val data: PhysicalData
) : Transaction {
    override fun clone(): TransactionImpl =
        copy(
            publicKey = publicKey,
            data = data.clone()
        )

    override fun serialize(encoder: BinaryFormat): ByteArray =
        encoder.dump(serializer(), this)

    override fun compareTo(other: Transaction): Int =
        data.compareTo(other.data)
}
