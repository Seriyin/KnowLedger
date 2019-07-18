package org.knowledger.ledger.config.chainid

import org.knowledger.ledger.config.adapters.ChainIdStorageAdapter
import org.knowledger.ledger.core.database.NewInstanceSession
import org.knowledger.ledger.core.database.StorageElement
import org.knowledger.ledger.core.database.StorageType
import org.knowledger.ledger.core.hash.Hash
import org.knowledger.ledger.core.results.Outcome
import org.knowledger.ledger.results.tryOrLedgerUnknownFailure
import org.knowledger.ledger.service.adapters.ServiceStorageAdapter
import org.knowledger.ledger.service.results.LedgerFailure

object SUChainIdStorageAdapter : ServiceStorageAdapter<StorageUnawareChainId> {
    override val id: String
        get() = ChainIdStorageAdapter.id

    override val properties: Map<String, StorageType>
        get() = ChainIdStorageAdapter.properties

    override fun store(
        toStore: StorageUnawareChainId, session: NewInstanceSession
    ): StorageElement =
        session
            .newInstance(id)
            .setStorageProperty("tag", toStore.tag)
            .setHashProperty("ledgerHash", toStore.ledgerHash)
            .setHashProperty("hashId", toStore.hashId)


    override fun load(
        ledgerHash: Hash,
        element: StorageElement
    ): Outcome<StorageUnawareChainId, LedgerFailure> =
        tryOrLedgerUnknownFailure {
            val hash =
                element.getHashProperty("hashId")

            val ledger =
                element.getHashProperty("ledgerHash")

            assert(ledger.contentEquals(ledgerHash))

            Outcome.Ok(
                StorageUnawareChainId(
                    element.getStorageProperty("tag"),
                    ledger,
                    hash
                )
            )
        }
}