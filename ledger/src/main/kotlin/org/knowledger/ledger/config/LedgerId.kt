@file:UseSerializers(InstantSerializer::class, UUIDSerializer::class)

package org.knowledger.ledger.config

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.knowledger.ledger.core.hash.Hasher
import org.knowledger.ledger.core.hash.Hashing
import org.knowledger.ledger.core.serial.HashSerializable
import org.knowledger.ledger.core.serial.InstantSerializer
import org.knowledger.ledger.core.serial.UUIDSerializer
import org.knowledger.ledger.data.Hash
import org.knowledger.ledger.service.ServiceClass
import java.time.Instant
import java.util.*

@Serializable
data class LedgerId(
    val tag: String, override val hash: Hash
) : Hashing, ServiceClass {

    internal constructor(
        tag: String, hasher: Hasher,
        encoder: BinaryFormat
    ) : this(
        tag,
        generateLedgerHandleHash(hasher, encoder, tag)
    )

    @Serializable
    private data class LedgerBuilder(
        val tag: String, val uuid: UUID,
        val instant: Instant
    ) : HashSerializable {
        override fun serialize(encoder: BinaryFormat): ByteArray =
            encoder.dump(serializer(), this)
    }

    companion object {
        private fun generateLedgerHandleHash(
            hasher: Hasher, encoder: BinaryFormat,
            tag: String
        ): Hash =
            LedgerBuilder(
                tag, UUID.randomUUID(),
                Instant.now()
            ).digest(hasher, encoder)
    }
}