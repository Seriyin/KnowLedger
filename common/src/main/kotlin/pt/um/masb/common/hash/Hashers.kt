package pt.um.masb.common.hash

import org.bouncycastle.jce.provider.BouncyCastleProvider
import pt.um.masb.common.misc.bytes
import pt.um.masb.common.misc.flattenBytes
import java.security.MessageDigest
import java.security.Security


sealed class AvailableHashAlgorithms : Hasher {
    class NoSuchHasherRegistered : Exception()

    object SHA256Hasher : AvailableHashAlgorithms() {

        val digester: MessageDigest by lazy {
            MessageDigest.getInstance(
                "SHA-256"
            )
        }

        override fun applyHash(input: ByteArray): Hash =
            Hash(digester.digest(input))


        override val hashSize: Long = 32

        override val id: Hash by lazy {
            val provider = digester.provider

            Hash(
                digester.digest(
                    flattenBytes(
                        digester.algorithm.toByteArray(),
                        provider.name.toByteArray(),
                        provider.version.bytes()
                    )
                )
            )
        }
    }

    companion object {
        init {
            //Ensure Bouncy Castle Crypto provider is present
            if (Security.getProvider("BC") == null) {
                Security.addProvider(
                    BouncyCastleProvider()
                )
            }
        }

        fun getHasher(hash: Hash): Hasher =
            when {
                SHA256Hasher.checkForCrypter(hash) -> SHA256Hasher
                else -> throw NoSuchHasherRegistered()
            }
    }
}

