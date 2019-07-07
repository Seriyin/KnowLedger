package org.knowledger.ledger.storage

import org.knowledger.common.Sizeable
import org.knowledger.common.storage.LedgerContract
import org.knowledger.ledger.data.MerkleTree
import java.util.*

interface Block : Sizeable, LedgerContract {
    val data: SortedSet<Transaction>
    val coinbase: Coinbase
    val header: BlockHeader
    var merkleTree: MerkleTree

    /**
     * Add a single new transaction.
     *
     * Checks if block is sized correctly.
     *
     * Checks if the transaction is valid.
     *
     * @param transaction   Transaction to attempt to add to the block.
     * @return Whether the transaction was valid and cprrectly inserted.
     */
    operator fun plus(transaction: Transaction): Boolean

    fun verifyTransactions(): Boolean

}
