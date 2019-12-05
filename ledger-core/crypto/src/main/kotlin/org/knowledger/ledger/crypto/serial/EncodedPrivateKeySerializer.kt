package org.knowledger.ledger.crypto.serial

import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.internal.StringDescriptor
import kotlinx.serialization.withName
import org.knowledger.ledger.core.base.hash.bytesFromHexString
import org.knowledger.ledger.crypto.EncodedPrivateKey
import org.knowledger.ledger.crypto.hash.toHexString

object EncodedPrivateKeySerializer : KSerializer<EncodedPrivateKey> {
    override val descriptor: SerialDescriptor =
        StringDescriptor.withName("EncodedPrivateKey")

    override fun deserialize(decoder: Decoder): EncodedPrivateKey =
        EncodedPrivateKey(decoder.decodeString().bytesFromHexString())

    override fun serialize(encoder: Encoder, obj: EncodedPrivateKey) =
        encoder.encodeString(obj.toHexString())
}