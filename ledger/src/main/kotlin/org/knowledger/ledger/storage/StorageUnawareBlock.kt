package org.knowledger.ledger.storage

import com.squareup.moshi.JsonClass
import org.knowledger.common.Sizeable
import org.knowledger.common.data.Difficulty
import org.knowledger.common.hash.Hash
import org.knowledger.common.storage.LedgerContract
import org.knowledger.ledger.config.BlockParams
import org.knowledger.ledger.config.ChainId
import org.knowledger.ledger.data.MerkleTree
import org.knowledger.ledger.service.handles.LedgerHandle
import org.openjdk.jol.info.ClassLayout
import org.tinylog.kotlin.Logger
import java.util.*

@JsonClass(generateAdapter = true)
data class StorageUnawareBlock(
    override val data: SortedSet<Transaction>,
    override val coinbase: Coinbase,
    override val header: BlockHeader,
    override var merkleTree: MerkleTree
) : Block, Sizeable, LedgerContract {

    @Transient
    private val classSize: Long =
        ClassLayout
            .parseClass(this::class.java)
            .instanceSize()

    @Transient
    private var headerSize: Long = 0

    @Transient
    private var transactionsSize: Long = 0

    //Consider only the class size contribution to size.
    //Makes the total block size in the possible
    // ballpark of 2MB + merkleRoot graph size.
    @Transient
    private var merkleTreeSize: Long =
        ClassLayout
            .parseClass(MerkleTree::class.java)
            .instanceSize()


    override val approximateSize: Long
        get() = classSize +
                transactionsSize +
                headerSize +
                merkleTreeSize


    constructor(
        chainId: ChainId,
        previousHash: Hash,
        difficulty: Difficulty,
        blockheight: Long,
        params: BlockParams
    ) : this(
        sortedSetOf(),
        Coinbase(LedgerHandle.getContainer(chainId.ledgerHash)!!),
        BlockHeader(
            chainId,
            LedgerHandle.getHasher(chainId.ledgerHash)!!,
            previousHash,
            difficulty,
            blockheight,
            params
        ),
        MerkleTree(LedgerHandle.getHasher(chainId.ledgerHash)!!)
    ) {
        headerSize = header.approximateSize
    }

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
    override fun plus(transaction: Transaction): Boolean {
        val transactionSize = transaction.approximateSize
        if (approximateSize +
            transactionSize < header.params.blockMemSize
        ) {
            if (data.size < header.params.blockLength) {
                if (transaction.processTransaction()) {
                    data.add(transaction)
                    transactionsSize += transactionSize
                    Logger.info {
                        "Transaction Successfully added to Block"
                    }
                    return true
                }
            }
        }
        Logger.info {
            "Transaction failed to process. Discarded."
        }
        return false
    }

    /**
     * Recalculates the entire block size.
     *
     * Is somewhat time consuming and only necessary if:
     *
     * 1. There is a need to calculate the effective block size after deserialization;
     * 2. There is a need to calculate the effective block size after retrieval
     *         of a block from a database.
     */
    fun resetApproximateSize() {
        headerSize = header.approximateSize
        transactionsSize = data.fold(
            0.toLong()
        ) { acc, transaction ->
            acc + transaction.approximateSize
        }
        merkleTreeSize = merkleTree.approximateSize
    }

    override fun verifyTransactions(): Boolean {
        return merkleTree.verifyBlockTransactions(
            coinbase,
            data.toTypedArray()
        )
    }

}