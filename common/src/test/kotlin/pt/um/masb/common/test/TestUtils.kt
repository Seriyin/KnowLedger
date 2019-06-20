package pt.um.masb.common.test

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.tinylog.kotlin.Logger
import pt.um.masb.common.hash.Hash
import pt.um.masb.common.misc.bytes
import pt.um.masb.common.misc.flattenBytes
import pt.um.masb.common.misc.hashFromHexString
import pt.um.masb.common.misc.hexString


class TestUtils {
    @Test
    fun `bytes conversions`() {
        val lsize = Long.SIZE_BYTES
        val test0 = 0L
        val test33 = 33L
        val test512 = 512L
        val test33564286 = 33564286L
        val bytes0 = ByteArray(lsize) {
            0x00
        }
        val bytes33 = when (lsize) {
            4 -> byteArrayOf(0x00, 0x00, 0x00, 0x21)
            8 -> byteArrayOf(0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x21)
            16 -> byteArrayOf(
                0x00,
                0x00,
                0x00,
                0x00,
                0x00,
                0x00,
                0x00,
                0x00,
                0x00,
                0x00,
                0x00,
                0x00,
                0x00,
                0x00,
                0x00,
                0x21
            )
            else -> fail {
                "Unexpected Long Size: $lsize"
            }
        }
        val bytes512 = when (lsize) {
            4 -> byteArrayOf(0x00, 0x00, 0x02, 0x00)
            8 -> byteArrayOf(0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x02, 0x00)
            16 -> byteArrayOf(
                0x00,
                0x00,
                0x00,
                0x00,
                0x00,
                0x00,
                0x00,
                0x00,
                0x00,
                0x00,
                0x00,
                0x00,
                0x00,
                0x00,
                0x02,
                0x00
            )
            else -> fail {
                "Unexpected Long Size: $lsize"
            }
        }
        val bytes33564286 = when (lsize) {
            4 -> byteArrayOf(0x02, 0x00, 0x26, 0x7E)
            8 -> byteArrayOf(0x00, 0x00, 0x00, 0x00, 0x02, 0x00, 0x26, 0x7E)
            16 -> byteArrayOf(
                0x00,
                0x00,
                0x00,
                0x00,
                0x00,
                0x00,
                0x00,
                0x00,
                0x00,
                0x00,
                0x00,
                0x00,
                0x02,
                0x00,
                0x26,
                0x7E
            )
            else -> fail {
                "Unexpected Long Size: $lsize"
            }
        }
        assertThat(test0.bytes()).containsExactly(*bytes0)
        assertThat(test33.bytes()).containsExactly(*bytes33)
        assertThat(test512.bytes()).containsExactly(*bytes512)
        assertThat(test33564286.bytes()).containsExactly(*bytes33564286)
    }

    @Nested
    inner class HashingOperations {
        val randomHashes = Array(randomInt(248) + 8) {
            Hash(randomByteArray(32))
        }

        @Test
        fun `byte flattening`() {
            Logger.debug {
                """Expected: ${randomHashes.joinToString {
                    it.bytes.hexString
                }}"""
            }

            val expected = randomHashes.reduce { acc, bytes ->
                acc + bytes
            }
            val expectedShort = expected.bytes.sliceArray(0..255)
            val test = flattenBytes(
                *randomHashes.map {
                    it.bytes
                }.toTypedArray()
            )
            val test2 = flattenBytes(
                randomHashes.sliceArray(0..6).map {
                    it.bytes
                },
                randomHashes[7].bytes
            )
            val test3 = flattenBytes(
                randomHashes.map {
                    it.bytes
                }.toTypedArray()
            )
            assertThat(test.size).isEqualTo(expected.bytes.size)
            assertThat(test2.size).isEqualTo(expectedShort.size)
            assertThat(test3.size).isEqualTo(expected.bytes.size)
            Logger.debug {
                """
                |
                |Expected:
                |   - ${expected.print}
                |Flatten via vararg byte arrays:
                |   - ${test.hexString}
            """.trimMargin()
            }
            assertThat(test).containsExactly(*expected.bytes)
            Logger.debug {
                """
                |
                |Expected:
                |   - ${expectedShort.hexString}
                |Flatten via collection + vararg byte arrays:
                |   - ${test2.hexString}
            """.trimMargin()
            }
            assertThat(test2).containsExactly(*expectedShort)
            Logger.debug {
                """
                |
                |Expected:
                |   - ${expected.print}
                |Flatten via direct AoA:
                |   - ${test3.hexString}
            """.trimMargin()
            }
            assertThat(test3).containsExactly(*expected.bytes)
        }

        @Test
        fun `pair crypting balanced`() {
            val expected = crypter.applyHash(
                crypter.applyHash(
                    crypter.applyHash(
                        randomHashes[0] + randomHashes[1]
                    ) + crypter.applyHash(
                        randomHashes[2] + randomHashes[3]
                    )
                ) + crypter.applyHash(
                    crypter.applyHash(
                        randomHashes[4] + randomHashes[5]
                    ) + crypter.applyHash(
                        randomHashes[6] + randomHashes[7]
                    )
                )
            )

            val test = applyHashInPairs(
                crypter,
                arrayOf(
                    randomHashes[0],
                    randomHashes[1],
                    randomHashes[2],
                    randomHashes[3],
                    randomHashes[4],
                    randomHashes[5],
                    randomHashes[6],
                    randomHashes[7]
                )
            )

            Logger.debug {
                """
                |
                | Test: ${test.print}
                | Expected: ${expected.print}
            """.trimMargin()
            }

            assertThat(expected.bytes).containsExactly(*test.bytes)

        }

        @Test
        fun `pair crypting unbalanced`() {
            val expected = crypter.applyHash(
                crypter.applyHash(
                    crypter.applyHash(
                        randomHashes[0] + randomHashes[1]
                    ) + crypter.applyHash(
                        randomHashes[2] + randomHashes[3]
                    )
                ) + crypter.applyHash(
                    crypter.applyHash(
                        randomHashes[4] + randomHashes[5]
                    ) + crypter.applyHash(
                        randomHashes[4] + randomHashes[5]
                    )
                )
            )

            val test = applyHashInPairs(
                crypter,
                arrayOf(
                    randomHashes[0],
                    randomHashes[1],
                    randomHashes[2],
                    randomHashes[3],
                    randomHashes[4],
                    randomHashes[5]
                )
            )

            Logger.info {
                """
                |
                | Test: ${test.print}
                | Expected: ${expected.print}
            """.trimMargin()
            }


            assertThat(expected.bytes).containsExactly(*test.bytes)
        }
    }

    @Nested
    inner class StringOperations {
        val canonicHex: (ByteArray) -> String = {
            val sb = StringBuilder(it.size * 2)
            for (b in it) {
                sb.append(String.format("%02x", b).toUpperCase())
            }
            sb.toString()
        }

        val canonicParse: (String) -> ByteArray = {
            val data = ByteArray(it.length / 2)
            var i = 0
            while (i < it.length) {
                data[i / 2] = Integer.decode("0x" + it[i] + it[i + 1]).toByte()
                i += 2
            }
            data
        }

        @Test
        fun `hex string printing and parsing`() {
            repeat(200) {
                val testHash = crypter.applyHash(randomByteArray(256))
                val expectedString = canonicHex(testHash.bytes)

                assertThat(testHash.print)
                    .isEqualTo(expectedString)

                assertThat(testHash.print.hashFromHexString.bytes)
                    .isEqualTo(canonicParse(expectedString))
            }
        }
    }
}