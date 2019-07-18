package org.knowledger.ledger.core.results

/**
 * Reserved for direct irrecoverable errors.
 * Query failures will wrap exceptions if thrown.
 */
interface HardFailure : Failable {
    override val cause: String
    val exception: Exception?
}