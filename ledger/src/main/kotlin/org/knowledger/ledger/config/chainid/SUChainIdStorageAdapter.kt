package org.knowledger.ledger.config.chainid

import org.knowledger.ledger.config.adapters.ChainIdStorageAdapter
import org.knowledger.ledger.core.results.Outcome
import org.knowledger.ledger.crypto.hash.Hash
import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.StorageType
import org.knowledger.ledger.results.tryOrLedgerUnknownFailure
import org.knowledger.ledger.service.adapters.ServiceStorageAdapter
import org.knowledger.ledger.service.results.LedgerFailure

internal object SUChainIdStorageAdapter : ServiceStorageAdapter<ChainIdImpl> {
    override val id: String
        get() = ChainIdStorageAdapter.id

    override val properties: Map<String, StorageType>
        get() = ChainIdStorageAdapter.properties

    override fun store(
        toStore: ChainIdImpl, session: ManagedSession
    ): StorageElement =
        session
            .newInstance(id)
            .setHashProperty("tag", toStore.tag)
            .setHashProperty("ledgerHash", toStore.ledgerHash)
            .setHashProperty("hash", toStore.hash)


    override fun load(
        ledgerHash: Hash,
        element: StorageElement
    ): Outcome<ChainIdImpl, LedgerFailure> =
        tryOrLedgerUnknownFailure {
            val hash: Hash =
                element.getHashProperty("hash")

            val ledger: Hash =
                element.getHashProperty("ledgerHash")

            val tag: Hash =
                element.getHashProperty("tag")


            assert(ledger == ledgerHash)

            Outcome.Ok(
                ChainIdImpl(
                    tag, ledger, hash
                )
            )
        }
}