package pt.um.masb.ledger.test

import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import mu.KLogger
import org.junit.jupiter.api.fail
import pt.um.masb.common.data.BlockChainData
import pt.um.masb.common.data.Payout
import pt.um.masb.common.database.ManagedDatabase
import pt.um.masb.common.database.orient.OrientDatabase
import pt.um.masb.common.database.orient.OrientDatabaseInfo
import pt.um.masb.common.database.orient.OrientDatabaseMode
import pt.um.masb.common.database.orient.OrientDatabaseType
import pt.um.masb.common.hash.Hash.Companion.emptyHash
import pt.um.masb.common.storage.LedgerContract
import pt.um.masb.common.test.randomDouble
import pt.um.masb.ledger.data.PhysicalData
import pt.um.masb.ledger.data.TUnit
import pt.um.masb.ledger.data.TemperatureData
import pt.um.masb.ledger.data.TrafficFlowData
import pt.um.masb.ledger.json.BigDecimalJsonAdapter
import pt.um.masb.ledger.json.BigIntegerJsonAdapter
import pt.um.masb.ledger.json.HashJsonAdapter
import pt.um.masb.ledger.json.InstantJsonAdapter
import pt.um.masb.ledger.json.PublicKeyJsonAdapter
import pt.um.masb.ledger.service.Identity
import pt.um.masb.ledger.service.ServiceHandle
import pt.um.masb.ledger.service.results.LedgerResult
import pt.um.masb.ledger.service.results.LoadListResult
import pt.um.masb.ledger.service.results.LoadResult
import pt.um.masb.ledger.storage.Coinbase
import pt.um.masb.ledger.storage.Transaction
import pt.um.masb.ledger.storage.TransactionOutput
import java.math.BigDecimal

internal val moshi by lazy {
    Moshi
        .Builder()
        .add(HashJsonAdapter())
        .add(PublicKeyJsonAdapter())
        .add(InstantJsonAdapter())
        .add(BigDecimalJsonAdapter())
        .add(BigIntegerJsonAdapter())
        .add(
            PolymorphicJsonAdapterFactory
                .of(BlockChainData::class.java, "type")
                .withSubtype(TemperatureData::class.java, "Temperature")
                .withSubtype(TrafficFlowData::class.java, "TrafficFlowData")
        )
        .build()
}

internal fun testDB(): ManagedDatabase = OrientDatabase(
    OrientDatabaseInfo(
        modeOpenOrient = OrientDatabaseMode.MEMORY,
        path = "",
        mode = OrientDatabaseType.MEMORY
    )
)

internal fun makeXTransactions(
    id: Array<Identity>,
    size: Int
): List<Transaction> {
    val ts: MutableList<Transaction> = mutableListOf()
    for (i in 0 until size) {
        val index = i % id.size
        ts.add(
            Transaction(
                id[index].privateKey,
                id[index].publicKey,
                PhysicalData(
                    TemperatureData(
                        BigDecimal(
                            randomDouble() * 100
                        ),
                        TUnit.CELSIUS
                    )
                )
            )
        )
    }
    return ts
}

internal fun makeXTransactions(
    id: Identity,
    size: Int
): List<Transaction> {
    val ts: MutableList<Transaction> = mutableListOf()
    for (i in 0 until size) {
        ts.add(
            Transaction(
                id.privateKey,
                id.publicKey,
                PhysicalData(
                    TemperatureData(
                        BigDecimal(
                            randomDouble() * 100
                        ),
                        TUnit.CELSIUS
                    )
                )
            )
        )
    }
    return ts
}


internal fun generateCoinbase(
    id: Array<Identity>,
    ts: List<Transaction>
): Coinbase {
    val sets = listOf(
        TransactionOutput(
            id[0].publicKey,
            emptyHash,
            Payout(BigDecimal.ONE),
            ts[0].hashId,
            emptyHash
        ),
        TransactionOutput(
            id[1].publicKey,
            emptyHash,
            Payout(BigDecimal.ONE),
            ts[1].hashId,
            emptyHash
        )
    )
    //First transaction output has
    //transaction 0.
    //Second is transaction 2
    //referencing transaction 0.
    //Third is transaction 4
    //referencing transaction 0.
    sets[0].addToPayout(
        Payout(BigDecimal.ONE),
        ts[2].hashId,
        ts[0].hashId
    )
    sets[0].addToPayout(
        Payout(BigDecimal.ONE),
        ts[4].hashId,
        ts[0].hashId
    )
    return Coinbase(
        sets.toSet() as MutableSet<TransactionOutput>,
        Payout(BigDecimal("3")),
        emptyHash
    )
}

internal fun logActualToExpectedLists(
    explanationActual: String,
    actualList: List<Any>,
    explanationExpected: String,
    expectedList: List<Any>,
    logger: KLogger
) {
    logger.info {
        """
            |
            |$explanationActual
            |${actualList.joinToString(
            """,

            """.trimIndent()
        ) { it.toString() }}
            |
            |$explanationExpected
            |${expectedList.joinToString(
            """,

            """.trimIndent()
        ) { it.toString() }}
        """.trimMargin()
    }
}


internal inline fun <T : LedgerContract> LoadListResult<T>.applyOrFail(
    block: List<T>.() -> Unit
) =
    when (this) {
        is LoadListResult.Success -> this.data.block()
        is LoadListResult.QueryFailure ->
            if (exception != null)
                fail(cause, exception)
            else
                fail(cause)
        is LoadListResult.NonExistentData -> fail(cause)
        is LoadListResult.NonMatchingCrypter -> fail(cause)
        is LoadListResult.UnrecognizedDataType -> fail(cause)
        is LoadListResult.Propagated -> fail(cause)
    }


internal inline fun <T : LedgerContract> LoadResult<T>.applyOrFail(
    block: T.() -> Unit
) =
    when (this) {
        is LoadResult.Success -> this.data.block()
        is LoadResult.QueryFailure ->
            if (exception != null)
                fail(cause, exception)
            else
                fail(cause)
        is LoadResult.NonExistentData -> fail(cause)
        is LoadResult.NonMatchingCrypter -> fail(cause)
        is LoadResult.UnrecognizedDataType -> fail(cause)
        is LoadResult.Propagated -> fail(cause)
    }


internal inline fun <T : ServiceHandle> LedgerResult<T>.applyOrFail(
    block: T.() -> Unit
) =
    when (this) {
        is LedgerResult.Success -> this.data.block()
        is LedgerResult.QueryFailure ->
            if (exception != null)
                fail(cause, exception)
            else
                fail(cause)
        is LedgerResult.NonExistentData -> fail(cause)
        is LedgerResult.NonMatchingCrypter -> fail(cause)
        is LedgerResult.Propagated -> fail(cause)
    }


internal fun <T : ServiceHandle> LedgerResult<T>.extractOrFail(): T =
    when (this) {
        is LedgerResult.Success -> this.data
        is LedgerResult.QueryFailure ->
            if (exception != null)
                fail(cause, exception)
            else
                fail(cause)
        is LedgerResult.NonExistentData -> fail(cause)
        is LedgerResult.NonMatchingCrypter -> fail(cause)
        is LedgerResult.Propagated -> fail(cause)
    }

internal fun StringBuilder.appendByLine(toPrint: Collection<String>): StringBuilder {
    for (thing in toPrint) {
        append(System.lineSeparator())
        append('\t')
        append(thing)
        append(',')
    }
    return this
}