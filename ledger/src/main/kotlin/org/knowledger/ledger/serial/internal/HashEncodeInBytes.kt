package org.knowledger.ledger.serial.internal

import kotlinx.serialization.CompositeDecoder
import kotlinx.serialization.CompositeEncoder
import kotlinx.serialization.SerialDescriptor
import org.knowledger.ledger.core.serial.HashSerializer
import org.knowledger.ledger.crypto.Hash

internal interface HashEncodeInBytes : HashEncode {
    override val hashDescriptor: SerialDescriptor
        get() = HashSerializer.descriptor

    override fun CompositeEncoder.encodeHash(index: Int, hash: Hash) {
        encodeSerializableElement(
            descriptor, index,
            HashSerializer, hash
        )
    }

    override fun CompositeDecoder.decodeHash(index: Int): Hash =
        decodeSerializableElement(
            descriptor, index,
            HashSerializer
        )

}