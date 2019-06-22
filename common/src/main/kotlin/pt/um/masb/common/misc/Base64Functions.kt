package pt.um.masb.common.misc

import pt.um.masb.common.hash.AvailableHashAlgorithms
import pt.um.masb.common.hash.Hash
import java.security.Key
import java.util.*

private val b64Encoder = Base64.getUrlEncoder()
private val b64Decoder = Base64.getUrlDecoder()


fun base64Encode(
    toEncode: Hash
): String =
    b64Encoder.encodeToString(toEncode.bytes)


fun base64Encode(
    toEncode: ByteArray
): String =
    b64Encoder.encodeToString(toEncode)

fun base64Decode(
    toDecode: String
): ByteArray =
    b64Decoder.decode(toDecode)

fun base64DecodeToHash(
    toDecode: String
): Hash =
    Hash(b64Decoder.decode(toDecode))

fun getStringFromKey(
    key: Key
): String =
    base64Encode(key.encoded)

fun <T> extractIdFromClass(clazz: Class<T>): String =
    base64Encode(
        AvailableHashAlgorithms.SHA256Hasher.applyHash(
            clazz.toGenericString()
        )
    )