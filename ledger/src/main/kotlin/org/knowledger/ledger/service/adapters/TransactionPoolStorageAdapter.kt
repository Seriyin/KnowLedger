package org.knowledger.ledger.service.adapters

import org.knowledger.ledger.core.database.NewInstanceSession
import org.knowledger.ledger.core.database.StorageElement
import org.knowledger.ledger.core.database.StorageType
import org.knowledger.ledger.core.hash.Hash
import org.knowledger.ledger.core.results.Outcome
import org.knowledger.ledger.results.deadCode
import org.knowledger.ledger.service.pools.transaction.SATransactionPoolStorageAdapter
import org.knowledger.ledger.service.pools.transaction.SUTransactionPoolStorageAdapter
import org.knowledger.ledger.service.pools.transaction.StorageAwareTransactionPool
import org.knowledger.ledger.service.pools.transaction.TransactionPool
import org.knowledger.ledger.service.pools.transaction.TransactionPoolImpl
import org.knowledger.ledger.service.results.LedgerFailure

object TransactionPoolStorageAdapter : ServiceStorageAdapter<TransactionPool> {
    override val id: String
        get() = "TransactionPool"

    override val properties: Map<String, StorageType>
        get() = mapOf(
            "chainId" to StorageType.LINK,
            "transactions" to StorageType.LIST,
            "confirmations" to StorageType.LIST
        )

    override fun store(
        toStore: TransactionPool,
        session: NewInstanceSession
    ): StorageElement =
        when (toStore) {
            is StorageAwareTransactionPool ->
                SATransactionPoolStorageAdapter.store(toStore, session)
            is TransactionPoolImpl ->
                SUTransactionPoolStorageAdapter.store(toStore, session)
            else -> deadCode()
        }

    override fun load(
        ledgerHash: Hash, element: StorageElement
    ): Outcome<TransactionPool, LedgerFailure> =
        SATransactionPoolStorageAdapter.load(ledgerHash, element)
}