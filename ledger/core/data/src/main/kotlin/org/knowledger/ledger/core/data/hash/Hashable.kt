package org.knowledger.ledger.core.data.hash

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.ExperimentalSerializationApi

/**
 * Indicates capability to produce
 * a unique digest of itself.
 */
interface Hashable {
    /**
     * Pure function that must produce a unique digest
     * through the use of a [Hasher] instance and
     * a [BinaryFormat] encoder.
     */
    @OptIn(ExperimentalSerializationApi::class)
    fun digest(hasher: Hasher, encoder: BinaryFormat): Hash
}