package org.knowledger.ledger.service.adapters

import org.knowledger.common.database.NewInstanceSession
import org.knowledger.common.database.StorageElement
import org.knowledger.common.database.StorageType
import org.knowledger.common.hash.Hash
import org.knowledger.common.results.Outcome
import org.knowledger.common.results.mapFailure
import org.knowledger.common.results.zip
import org.knowledger.ledger.config.adapters.CoinbaseParamsStorageAdapter
import org.knowledger.ledger.config.adapters.LedgerIdStorageAdapter
import org.knowledger.ledger.config.adapters.LedgerParamsStorageAdapter
import org.knowledger.ledger.results.intoHandle
import org.knowledger.ledger.results.tryOrHandleUnknownFailure
import org.knowledger.ledger.service.LedgerConfig
import org.knowledger.ledger.service.handles.LedgerHandle

object LedgerConfigStorageAdapter : HandleStorageAdapter {

    override val id: String
        get() = "LedgerConfig"

    override val properties: Map<String, StorageType>
        get() = mapOf(
            "ledgerId" to StorageType.LINK,
            "ledgerParams" to StorageType.LINK,
            "coinbaseParams" to StorageType.LINK
        )

    override fun store(
        toStore: LedgerConfig, session: NewInstanceSession
    ): StorageElement =
        session
            .newInstance(id)
            .setLinked(
                "ledgerId", LedgerIdStorageAdapter,
                toStore.ledgerId, session
            ).setLinked(
                "ledgerParams", LedgerParamsStorageAdapter,
                toStore.ledgerParams, session
            ).setLinked(
                "coinbaseParams", CoinbaseParamsStorageAdapter,
                toStore.coinbaseParams, session
            )


    override fun load(
        ledgerHash: Hash,
        element: StorageElement
    ): Outcome<LedgerConfig, LedgerHandle.Failure> =
        tryOrHandleUnknownFailure {
            val ledger = element.getLinked("ledgerId")
            val ledgerP = element.getLinked("ledgerParams")
            val coinbaseParams = element.getLinked("coinbaseParams")
            zip(
                LedgerIdStorageAdapter.load(ledgerHash, ledger),
                LedgerParamsStorageAdapter.load(ledgerHash, ledgerP),
                CoinbaseParamsStorageAdapter.load(
                    ledgerHash, coinbaseParams
                )
            ) { ledgerId, ledgerParams, coinbaseParams ->
                LedgerConfig(
                    ledgerId,
                    ledgerParams,
                    coinbaseParams
                )
            }.mapFailure {
                it.intoHandle()
            }
        }
}