package org.knowledger.ledger.core.base.data

import java.math.BigInteger

/**
 * Difficulty is a parameter for **block committal speed**.
 * A block is mined against a difficulty, which directly
 * correlates with the speed to mining completion.
 */
data class Difficulty(
    val difficulty: BigInteger
) {
    operator fun compareTo(hashTarget: Difficulty): Int =
        difficulty.compareTo(hashTarget.difficulty)

    val print: String
        get() = difficulty.toString(HEXR)

    companion object {
        const val HEXR = 16
        const val SIZE = 32

        val MAX_DIFFICULTY: Difficulty =
            Difficulty(
                BigInteger(
                    ByteArray(SIZE) {
                        0xFF.toByte()
                    }.apply {
                        this[0] = 0x7F.toByte()
                    }
                )
            )

        val INIT_DIFFICULTY: Difficulty =
            Difficulty(
                BigInteger(
                    ByteArray(SIZE).apply {
                        this[0] = 0x04.toByte()
                    }
                )
            )

        val MIN_DIFFICULTY: Difficulty =
            Difficulty(BigInteger.ZERO)
    }
}