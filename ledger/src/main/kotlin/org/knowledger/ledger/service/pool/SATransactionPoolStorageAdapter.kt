package org.knowledger.ledger.service.pool

import org.knowledger.common.database.NewInstanceSession
import org.knowledger.common.database.StorageElement
import org.knowledger.common.database.StorageType
import org.knowledger.common.hash.Hash
import org.knowledger.common.results.Outcome
import org.knowledger.ledger.adapters.cacheStore
import org.knowledger.ledger.adapters.cachedLoad
import org.knowledger.ledger.service.adapters.ServiceStorageAdapter
import org.knowledger.ledger.service.adapters.TransactionPoolStorageAdapter
import org.knowledger.ledger.service.results.LedgerFailure

object SATransactionPoolStorageAdapter :
    ServiceStorageAdapter<StorageAwareTransactionPool> {
    override val id: String
        get() = TransactionPoolStorageAdapter.id

    override val properties: Map<String, StorageType>
        get() = TransactionPoolStorageAdapter.properties

    override fun store(
        toStore: StorageAwareTransactionPool,
        session: NewInstanceSession
    ): StorageElement =
        session.cacheStore(
            SUTransactionPoolStorageAdapter,
            toStore, toStore.transactionPool
        )

    override fun load(
        ledgerHash: Hash, element: StorageElement
    ): Outcome<StorageAwareTransactionPool, LedgerFailure> =
        element.cachedLoad(ledgerHash, SUTransactionPoolStorageAdapter) {
            StorageAwareTransactionPool(it)
        }
}