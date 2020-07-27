@file:UseSerializers(HashSerializer::class, EncodedSignatureSerializer::class, PublicKeySerializer::class)

package org.knowledger.ledger.storage.transaction

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.knowledger.ledger.core.data.hash.Hash
import org.knowledger.ledger.core.serial.HashSerializer
import org.knowledger.ledger.crypto.EncodedSignature
import org.knowledger.ledger.crypto.serial.EncodedSignatureSerializer
import org.knowledger.ledger.crypto.serial.PublicKeySerializer
import org.knowledger.ledger.storage.PhysicalData
import java.security.PublicKey

@Serializable
data class ImmutableTransaction(
    override val hash: Hash,
    override val signature: EncodedSignature,
    override val publicKey: PublicKey,
    override val data: PhysicalData,
    override val approximateSize: Int = -1
) : HashedTransaction {
    override fun processTransaction(encoder: BinaryFormat): Boolean =
        verifySignature(encoder)
}

