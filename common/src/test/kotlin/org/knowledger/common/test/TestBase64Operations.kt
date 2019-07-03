package org.knowledger.common.test

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test
import org.knowledger.common.misc.base64Decode
import org.knowledger.common.misc.base64DecodeToString
import org.knowledger.common.misc.base64Encode
import org.tinylog.kotlin.Logger

class TestBase64Operations {
    @Test
    fun `base64 encoding decoding from string`() {
        val check = "thisissampletext"
        val base64 = base64Encode(check)
        val decoded = base64DecodeToString(base64)
        assertThat(
            base64
        ).isEqualTo(
            "dGhpc2lzc2FtcGxldGV4dA=="
        )
        assertThat(
            decoded
        ).isEqualTo(
            check
        )


        Logger.debug {
            """
                | 
                | Input: "thisissampletext"
                | Base64: $base64
                | Decoded: $decoded
            """.trimMargin()
        }
    }

    @ExperimentalStdlibApi
    @Test
    fun `base64 encoding decoding from UTF-8 bytes`() {
        val check = byteArrayOf(
            0x74, 0x68, 0x69, 0x73,
            0x69, 0x73, 0x73, 0x61,
            0x6d.toByte(), 0x70, 0x6c.toByte(), 0x65,
            0x74, 0x65, 0x78, 0x74
        )
        val base64 = base64Encode(check)
        val decoded = base64Decode(base64)
        assertThat(
            base64
        ).isEqualTo(
            "dGhpc2lzc2FtcGxldGV4dA=="
        )
        assertThat(
            decoded
        ).isEqualTo(
            check
        )

        Logger.debug {
            """
                | 
                | Input: ${check.decodeToString()}
                | Base64: $base64
                | Decoded: ${decoded.decodeToString()}
            """.trimMargin()
        }
    }
}