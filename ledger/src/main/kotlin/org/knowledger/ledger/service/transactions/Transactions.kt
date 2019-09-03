package org.knowledger.ledger.service.transactions

import org.knowledger.ledger.core.database.query.UnspecificQuery
import org.knowledger.ledger.core.hash.Hash
import org.knowledger.ledger.core.misc.base64Encoded
import org.knowledger.ledger.core.results.Outcome
import org.knowledger.ledger.service.results.LoadFailure
import org.knowledger.ledger.storage.adapters.TransactionStorageAdapter
import org.knowledger.ledger.storage.transaction.HashedTransaction
import java.security.PublicKey


// ------------------------------
// Transactions over transactions.
// Go figure.
//
// Execution must be runtime determined.
// ------------------------------
internal fun PersistenceWrapper.getTransactionsFromAgent(
    tag: String,
    publicKey: PublicKey
): Outcome<Sequence<HashedTransaction>, LoadFailure> =
    TransactionStorageAdapter.let {
        queryResults(
            UnspecificQuery(
                """
                    SELECT 
                    FROM ${it.id}
                    WHERE value.value.@class = :tag
                        AND publicKey = :publicKey
                """.trimIndent(),
                mapOf(
                    "tag" to tag,
                    "publicKey" to publicKey.encoded
                )
            ),
            it
        )
    }

internal fun PersistenceWrapper.getTransactionsFromAgent(
    tag: Hash,
    publicKey: PublicKey
): Outcome<Sequence<HashedTransaction>, LoadFailure> =
    getTransactionsFromAgent(
        tag.base64Encoded(), publicKey
    )


internal fun PersistenceWrapper.getTransactionByHash(
    tag: String,
    hash: Hash
): Outcome<HashedTransaction, LoadFailure> =
    TransactionStorageAdapter.let {
        queryUniqueResult(
            UnspecificQuery(
                """
                    SELECT 
                    FROM ${it.id}
                    WHERE value.value.@class = :tag
                        AND hash = :hash
                """.trimIndent(),
                mapOf(
                    "tag" to tag,
                    "hash" to hash.bytes
                )
            ),
            it
        )
    }

internal fun PersistenceWrapper.getTransactionByHash(
    tag: Hash,
    hash: Hash
): Outcome<HashedTransaction, LoadFailure> =
    getTransactionByHash(tag.base64Encoded(), hash)


//Execution must be runtime determined.
internal fun PersistenceWrapper.getTransactionsOrderedByTimestamp(
    tag: String
): Outcome<Sequence<HashedTransaction>, LoadFailure> =
    TransactionStorageAdapter.let {
        queryResults(
            UnspecificQuery(
                """
                    SELECT 
                    FROM ${it.id}
                    WHERE value.value.@class = :tag
                    ORDER BY value.seconds DESC, value.nanos DESC
                """.trimIndent(),
                mapOf(
                    "tag" to tag
                )
            ),
            it
        )

    }

//Execution must be runtime determined.
internal fun PersistenceWrapper.getTransactionsOrderedByTimestamp(
    tag: Hash
): Outcome<Sequence<HashedTransaction>, LoadFailure> =
    getTransactionsOrderedByTimestamp(tag.base64Encoded())


internal fun PersistenceWrapper.getTransactionsByClass(
    tag: String
): Outcome<Sequence<HashedTransaction>, LoadFailure> =
    TransactionStorageAdapter.let {
        queryResults(
            UnspecificQuery(
                """
                    SELECT 
                    FROM ${it.id}
                    WHERE value.value.@class = :tag
                """.trimIndent(),
                mapOf(
                    "tag" to tag
                )
            ),
            it
        )

    }

internal fun PersistenceWrapper.getTransactionsByClass(
    tag: Hash
): Outcome<Sequence<HashedTransaction>, LoadFailure> =
    getTransactionsByClass(tag.base64Encoded())
