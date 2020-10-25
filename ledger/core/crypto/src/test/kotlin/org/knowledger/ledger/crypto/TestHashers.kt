package org.knowledger.ledger.crypto

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.knowledger.encoding.base16.HexString
import org.knowledger.encoding.base16.hexEncoded
import org.knowledger.ledger.crypto.hash.Hashers
import org.tinylog.Logger

class TestHashers {
    val sample = "testByteArray".toByteArray()
    val fixed32 = ByteArray(32, Int::toByte)
    val fixed64 = ByteArray(64, Int::toByte)

    private fun assertHashAndLog(algorithm: String, test: HexString, expected: String) {
        assertThat(test).isEqualTo(expected)
        Logger.debug {
            """ $algorithm
                |test: $test
                |expected: $expected
            """.trimMargin()
        }
    }

    private fun assertHash(hashers: Hashers, algorithm: String, expected: String) {
        val test = hashers.applyHash(sample).hexEncoded()
        assertHashAndLog(algorithm, test, expected)
    }

    private fun assertFixed32Hash(hashers: Hashers, algorithm: String, expected: String) {
        val test = hashers.applyHash(fixed32).hexEncoded()
        assertHashAndLog(algorithm, test, expected)
    }

    private fun assertFixed64Hash(hashers: Hashers, algorithm: String, expected: String) {
        val test = hashers.applyHash(fixed64).hexEncoded()
        assertHashAndLog(algorithm, test, expected)
    }


    @Nested
    inner class SHA {
        @Test
        fun `apply SHA-256`() {
            val expected32 = "6EC7BBB8088AD0582BA4CCA03667C20BA7A58F5A6CBAB23706B6F4260CAECC5A"
            val hashers = Hashers.SHA256Hasher
            assertHash(hashers, hashers.algorithmTag, expected32)
        }

        @Test
        fun `apply SHA-512`() {
            val expected64 =
                "3ED19606CF37AD45E9E6D152EB30BE6813576237E08829AFC90312A1FD908214D7B3B7C194520AC1808375AB83A4CE65712C98995083A55704985F91C419963E"
            val hashers = Hashers.SHA512Hasher
            assertHash(hashers, hashers.algorithmTag, expected64)
        }
    }

    @Nested
    inner class HARAKA {
        @Test
        fun `apply HARAKA-256`() {
            val expected32 = "8027CCB87949774B78D0545FB72BF70C695C2A0923CBD47BBA1159EFBF2B2C1C"
            val hashers = Hashers.Haraka256Hasher
            assertFixed32Hash(hashers, hashers.algorithmTag, expected32)
        }

        @Test
        fun `apply HARAKA-512`() {
            val expected64 = "BE7F723B4E80A99813B292287F306F625A6D57331CAE5F34DD9277B0945BE2AA"
            val hashers = Hashers.Haraka512Hasher
            assertFixed64Hash(hashers, hashers.algorithmTag, expected64)
        }
    }

    @Nested
    inner class BLAKE2 {
        @Test
        fun `apply BLAKE2S-256`() {
            val expected32 = "71E7093BAC0CACE10B4E00ADFDFC48B6C4B5771D0B3522880C4F5F97BB551168"
            val hashers = Hashers.Blake2s256Hasher
            assertHash(hashers, hashers.algorithmTag, expected32)
        }

        /**
         * BEWARE: Not entirely verified externally to be matching some other reference implementation.
         */
        @Test
        fun `apply BLAKE2B-256`() {
            val expected32 = "6A1FAE02BBF98D1A96C99C38F616D4559E527E3CC1A719A996C093A7867C8FFA"
            val hashers = Hashers.Blake2b256Hasher
            assertHash(hashers, hashers.algorithmTag, expected32)
        }

        @Test
        fun `apply BLAKE2B-512`() {
            val expected64 =
                "9268B2FCE73C20D8E4CEFF7BB401968D77B0AA044F63025885E509714EA930F283E57D91919960B1EB573ED60EC303E15236469045F9570A44DB93A2AAE62405"
            val hashers = Hashers.Blake2b512Hasher
            assertHash(hashers, hashers.algorithmTag, expected64)
        }
    }


    @Nested
    inner class SHA3 {
        @Test
        fun `apply SHA3-256`() {
            val expected32 = "8F42B68F1B239CD0F9F51EABFD43DAE5775CB1531E1CC64444115D1582518283"
            val hashers = Hashers.SHA3256Hasher
            assertHash(hashers, hashers.algorithmTag, expected32)
        }

        @Test
        fun `apply SHA3-512`() {
            val expected64 =
                "19B24FE949322476C9131D16F55D240B3B3CA288A8E0F379250101C3698ED06C8AC134DD4A80576051159DD3641B1421491223934C1F4E54D297E8FFA147DFE2"
            val hashers = Hashers.SHA3512Hasher
            assertHash(hashers, hashers.algorithmTag, expected64)
        }
    }

    @Nested
    inner class KECCAK {
        /**
         * BEWARE: Not entirely verified externally to be matching some other reference implementation.
         */
        @Test
        fun `apply KECCAK-256`() {
            val expected32 = "89AE94189AAB48578D1610872CDE9C5F647BF29494B54C5B7D4C7655627E34B7"
            val hashers = Hashers.Keccak256Hasher
            assertHash(hashers, hashers.algorithmTag, expected32)
        }

        /**
         * BEWARE: Not entirely verified externally to be matching some other reference implementation.
         */
        @Test
        fun `apply KECCAK-512`() {
            val expected64 =
                "A473EA3FAF6BC58B04E41346CC729BFF2B1C8CCCF84CC183F97CB5AFF796EB49C071655D28B1C36A4A6819EB55CDCFBD920977717EEFB09BD2B34E6DFCE51138"
            val hashers = Hashers.Keccak512Hasher
            assertHash(hashers, hashers.algorithmTag, expected64)
        }
    }
}