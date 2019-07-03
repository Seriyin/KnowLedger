package org.knowledger.ledger.service.handles.builder

import org.knowledger.common.database.DatabaseMode
import org.knowledger.common.database.DatabaseType
import org.knowledger.common.database.ManagedDatabase
import org.knowledger.common.database.ManagedSession
import org.knowledger.common.database.orient.OrientDatabase
import org.knowledger.common.database.orient.OrientDatabaseInfo
import org.knowledger.common.hash.Hasher
import org.knowledger.common.misc.base64Encode
import org.knowledger.common.results.Outcome
import org.knowledger.common.results.mapSuccess
import org.knowledger.ledger.config.CoinbaseParams
import org.knowledger.ledger.config.LedgerId
import org.knowledger.ledger.config.LedgerParams
import org.knowledger.ledger.service.LedgerConfig
import org.knowledger.ledger.service.handles.LedgerHandle
import org.knowledger.ledger.service.transactions.PersistenceWrapper
import java.io.File

class LedgerByTag(
    val identity: String
) : AbstractLedgerBuilder(), LedgerBuilder<LedgerByTag> {
    private var ledgerParams: LedgerParams? = null
    private var coinbaseParams: CoinbaseParams? = null

    override fun withDBPath(
        path: File
    ): Outcome<LedgerByTag, LedgerHandle.Failure> =
        setDBPath(path).mapSuccess {
            this
        }

    override fun withDBPath(path: String): LedgerByTag =
        apply {
            this.path = path
        }

    fun withHasher(hasher: Hasher): LedgerByTag =
        apply {
            this.hasher = hasher
        }

    fun withCustomParams(
        ledgerParams: LedgerParams
    ): LedgerByTag =
        apply {
            this.ledgerParams = ledgerParams
        }

    fun withCoinbaseParams(
        coinbaseParams: CoinbaseParams
    ): LedgerByTag =
        apply {
            this.coinbaseParams = coinbaseParams
        }

    override fun withCustomSession(
        dbOpenMode: DatabaseMode, dbSessionType: DatabaseType,
        dbUser: String?, dbPassword: String?
    ): LedgerByTag = apply {
        setCustomSession(
            dbOpenMode, dbSessionType,
            dbUser, dbPassword
        )
    }

    override fun withCustomDB(db: ManagedDatabase): LedgerByTag =
        apply {
            this.db = db
        }

    internal fun withCustomDB(
        db: ManagedDatabase, session: ManagedSession
    ) =
        apply {
            setCustomDB(db, session)
        }


    private fun generateLedgerParams() {
        if (ledgerParams == null) {
            ledgerParams = LedgerParams(hasher.id)
        }
        if (coinbaseParams == null) {
            coinbaseParams = CoinbaseParams()
        }
    }

    private fun attemptToResolveId() {
            ledgerConfig = LedgerConfig(
                LedgerId(identity, hasher), ledgerParams!!,
                coinbaseParams!!
            )
    }

    private fun generateDB() {
        if (db == null) {
            db = OrientDatabase(
                OrientDatabaseInfo(
                    dbMode, dbType, path,
                    user = dbUser, password = dbPassword
                )
            )
        }
        if (session == null) {
            session = db?.newManagedSession(base64Encode(ledgerConfig.ledgerId.hashId))
        }
        persistenceWrapper = PersistenceWrapper(ledgerConfig.ledgerId.hashId, session!!)
        persistenceWrapper.registerDefaultSchemas()
    }

    override fun build(): Outcome<LedgerHandle, LedgerHandle.Failure> {
        generateLedgerParams()
        attemptToResolveId()
        generateDB()
        addToContainers()
        return Outcome.Ok(LedgerHandle(this))
    }

}