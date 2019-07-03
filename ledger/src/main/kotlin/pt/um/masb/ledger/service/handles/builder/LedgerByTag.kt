package pt.um.masb.ledger.service.handles.builder

import pt.um.masb.common.database.DatabaseMode
import pt.um.masb.common.database.DatabaseType
import pt.um.masb.common.database.ManagedDatabase
import pt.um.masb.common.database.ManagedSession
import pt.um.masb.common.database.orient.OrientDatabase
import pt.um.masb.common.database.orient.OrientDatabaseInfo
import pt.um.masb.common.hash.Hasher
import pt.um.masb.common.misc.base64Encode
import pt.um.masb.common.results.Outcome
import pt.um.masb.common.results.mapSuccess
import pt.um.masb.ledger.config.CoinbaseParams
import pt.um.masb.ledger.config.LedgerId
import pt.um.masb.ledger.config.LedgerParams
import pt.um.masb.ledger.service.LedgerConfig
import pt.um.masb.ledger.service.handles.LedgerHandle
import pt.um.masb.ledger.service.transactions.PersistenceWrapper
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