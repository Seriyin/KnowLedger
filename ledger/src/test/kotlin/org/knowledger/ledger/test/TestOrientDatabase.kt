package org.knowledger.ledger.test

import assertk.assertAll
import assertk.assertThat
import assertk.assertions.*
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.knowledger.collections.mapToSet
import org.knowledger.ledger.config.adapters.BlockParamsStorageAdapter
import org.knowledger.ledger.config.adapters.ChainIdStorageAdapter
import org.knowledger.ledger.config.adapters.LedgerIdStorageAdapter
import org.knowledger.ledger.config.adapters.LedgerParamsStorageAdapter
import org.knowledger.ledger.core.data.PhysicalData
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.crypto.service.Identity
import org.knowledger.ledger.data.TemperatureData
import org.knowledger.ledger.data.TrafficFlowData
import org.knowledger.ledger.data.adapters.DummyDataStorageAdapter
import org.knowledger.ledger.data.adapters.PhysicalDataStorageAdapter
import org.knowledger.ledger.data.adapters.TemperatureDataStorageAdapter
import org.knowledger.ledger.data.adapters.TrafficFlowDataStorageAdapter
import org.knowledger.ledger.database.DatabaseMode
import org.knowledger.ledger.database.DatabaseType
import org.knowledger.ledger.database.adapters.SchemaProvider
import org.knowledger.ledger.database.orient.OrientDatabase
import org.knowledger.ledger.database.orient.OrientDatabaseInfo
import org.knowledger.ledger.database.orient.OrientSession
import org.knowledger.ledger.database.query.UnspecificQuery
import org.knowledger.ledger.results.mapSuccess
import org.knowledger.ledger.results.unwrap
import org.knowledger.ledger.service.adapters.ChainHandleStorageAdapter
import org.knowledger.ledger.service.adapters.IdentityStorageAdapter
import org.knowledger.ledger.service.adapters.LedgerConfigStorageAdapter
import org.knowledger.ledger.service.adapters.PoolTransactionStorageAdapter
import org.knowledger.ledger.service.adapters.TransactionPoolStorageAdapter
import org.knowledger.ledger.service.handles.ChainHandle
import org.knowledger.ledger.service.handles.LedgerHandle
import org.knowledger.ledger.service.transactions.getTransactionByHash
import org.knowledger.ledger.service.transactions.getTransactionsByClass
import org.knowledger.ledger.service.transactions.getTransactionsFromAgent
import org.knowledger.ledger.service.transactions.getTransactionsOrderedByTimestamp
import org.knowledger.ledger.storage.adapters.BlockHeaderStorageAdapter
import org.knowledger.ledger.storage.adapters.BlockStorageAdapter
import org.knowledger.ledger.storage.adapters.CoinbaseStorageAdapter
import org.knowledger.ledger.storage.adapters.MerkleTreeStorageAdapter
import org.knowledger.ledger.storage.adapters.TransactionOutputStorageAdapter
import org.knowledger.ledger.storage.adapters.TransactionStorageAdapter
import org.knowledger.ledger.storage.transaction.HashedTransactionImpl
import org.knowledger.ledger.storage.transaction.StorageAwareTransaction
import org.knowledger.testing.ledger.appendByLine
import org.knowledger.testing.ledger.encoder
import org.knowledger.testing.ledger.failOnError
import org.knowledger.testing.ledger.logActualToExpectedLists
import org.knowledger.testing.ledger.queryToList
import org.tinylog.kotlin.Logger
import java.math.BigDecimal

class TestOrientDatabase {
    val id = Identity("test")

    val db = OrientDatabase(
        OrientDatabaseInfo(
            databaseMode = DatabaseMode.MEMORY,
            databaseType = DatabaseType.MEMORY,
            path = "test"
        )
    )
    val session = db.newManagedSession("test")
    val ledger = LedgerHandle
        .Builder()
        .withLedgerIdentity("test")
        .unwrap()
        .withCustomDB(db, session)
        .withLedgerSerializationModule {
            TemperatureData::class with TemperatureData.serializer()
            TrafficFlowData::class with TrafficFlowData.serializer()
        }
        .build()
        .unwrap()

    val hash = ledger.ledgerHash

    val temperatureChain: ChainHandle = ledger.registerNewChainHandleOf(
        TemperatureDataStorageAdapter
    ).unwrap()

    val chainId = temperatureChain.id
    val chainHash = temperatureChain.id.hash
    internal val pw = LedgerHandle.getContainer(hash)!!.persistenceWrapper
    val transactions = generateXTransactions(id, 20).toSortedSet()


    @BeforeAll
    fun `initialize DB`() {
        transactions.forEach {
            pw.persistEntity(it, TransactionStorageAdapter)
        }
    }

    @Nested
    inner class Session {
        val tid = TransactionStorageAdapter.id


        @Nested
        inner class Clusters {
            val adapters: List<SchemaProvider<out Any>> = listOf(
                //Configuration Adapters
                BlockParamsStorageAdapter,
                ChainIdStorageAdapter,
                CoinbaseStorageAdapter,
                LedgerConfigStorageAdapter,
                LedgerIdStorageAdapter,
                LedgerParamsStorageAdapter,
                //ServiceAdapters
                ChainHandleStorageAdapter,
                IdentityStorageAdapter,
                TransactionPoolStorageAdapter,
                PoolTransactionStorageAdapter,
                //StorageAdapters
                BlockHeaderStorageAdapter,
                BlockStorageAdapter,
                CoinbaseStorageAdapter,
                MerkleTreeStorageAdapter,
                PhysicalDataStorageAdapter,
                TransactionOutputStorageAdapter,
                TransactionStorageAdapter,
                //DataAdapters
                DummyDataStorageAdapter
            )

            @Test
            fun `created clusters`() {
                val plug = session as OrientSession
                val clusterNames = session.clustersPresent
                Logger.info {
                    StringBuilder()
                        .append(System.lineSeparator())
                        .append("Clusters present in ${plug.name}")
                        .appendByLine(clusterNames)
                        .toString()
                }
                assertThat(
                    clusterNames.mapToSet {
                        it.substringBeforeLast('_')
                    }
                ).containsAll(
                    *adapters.map {
                        it.id.toLowerCase()
                    }.toTypedArray()
                )
            }

            @Test
            fun `cluster query`() {
                //Query from first cluster.
                val elements = session.queryToList(
                    """
                        SELECT 
                        FROM CLUSTER:${tid}_1
                    """.trimIndent()
                )
                elements.forEach { res ->
                    Logger.info {
                        res.json
                    }
                }
                //Ensure there is a subset of the generated transactions
                //present.
                assertAll {
                    assertThat(elements.size).isLessThanOrEqualTo(transactions.size)
                    elements.map {
                        it.getHashProperty("hash")
                    }.forEach { hash ->
                        assertThat(transactions.map {
                            it.hash
                        }).contains(
                            hash
                        )
                    }
                }

            }

        }

        @Test
        fun `transaction all properties present`() {
            val orient = (session as OrientSession)
            val present = orient.browseClass(
                tid
            ).toList()
            assertThat(present.size).isEqualTo(transactions.size)
            val schemaProps =
                TransactionStorageAdapter.properties.keys.toTypedArray()
            assertAll {
                present.forEach {
                    assertThat(
                        it.presentProperties
                    ).isNotNull().containsAll(
                        *schemaProps
                    )
                }
            }
            Logger.info {
                StringBuilder("Properties in Transaction:")
                    .appendByLine(present[0].presentProperties)
                    .toString()
            }
            logActualToExpectedLists(
                "Transactions' hashes from DB:",
                present.map {
                    it.getHashProperty("hash").toHexString()
                },
                "Transactions' hashes from test:",
                transactions.map { it.hash.toHexString() }
            )
        }

        @Test
        fun `binary records`() {
            val elements = session.queryToList(
                """
                    SELECT 
                    FROM $tid
                """.trimIndent()
            )
            assertThat(elements.isNotEmpty()).isTrue()
            val binary = elements[0].getHashProperty("hash")
            assertThat(binary.bytes).containsExactly(
                *transactions.first().hash.bytes
            )
        }

        @Test
        fun `transaction with hash id`() {
            val elements = session.queryToList(
                UnspecificQuery(
                    """
                    SELECT
                    FROM $tid
                    WHERE hash = :hash
                    """.trimIndent(),
                    mapOf(
                        "hash" to transactions.first().hash.bytes
                    )
                )
            )
            assertThat(elements.isNotEmpty()).isTrue()
            val binary = elements[0].getHashProperty("hash")
            assertThat(binary.bytes).containsExactly(
                *transactions.first().hash.bytes
            )

        }
    }

    @Nested
    inner class Handles {
        val hasher: Hashers = LedgerHandle.getHasher(hash)!!

        val trafficChain: ChainHandle = ledger.registerNewChainHandleOf(
            TrafficFlowDataStorageAdapter
        ).unwrap()

        @Nested
        inner class Blocks {

            @Test
            fun `Test simple insertion`() {

                val block = generateBlockWithChain(
                    temperatureChain.id
                )
                assertThat(block + transactions.first())
                    .isTrue()
                assertThat(block.transactions.first())
                    .isNotNull()
                    .isEqualTo(transactions.first())
            }


            @Test
            fun `Test traffic insertion`() {
                val testTraffic = HashedTransactionImpl(
                    id.privateKey,
                    id.publicKey,
                    PhysicalData(
                        BigDecimal.ONE,
                        BigDecimal.ONE,
                        TrafficFlowData(
                            "FRC0",
                            20,
                            20,
                            3000,
                            3000,
                            34.5,
                            12.6
                        )
                    ),
                    hasher, encoder
                )

                val block = generateBlockWithChain(
                    trafficChain.id
                )
                assertThat(block).isNotNull()
                assertThat(block + testTraffic)
                    .isTrue()
                assertThat(block.transactions.first())
                    .isNotNull()
                    .isEqualTo(testTraffic)
            }

        }
    }

    @Nested
    inner class Persistence {

        @Test
        fun `loading transactions`() {
            pw.getTransactionsByClass(
                chainId.tag
            ).mapSuccess { seq ->
                seq.asTransactions().apply {
                    logActualToExpectedLists(
                        "Transactions' hashes from DB:",
                        map { it.hash.toHexString() },
                        "Transactions' hashes from test:",
                        transactions.map { it.hash.toHexString() }
                    )
                    assertThat(size).isEqualTo(
                        transactions.size
                    )
                    assertThat(this).containsOnly(
                        *transactions.toTypedArray()
                    )
                }
            }.failOnError()
        }

        @Test
        fun `loading transactions by timestamp`() {
            pw.getTransactionsOrderedByTimestamp(
                chainId.tag
            ).mapSuccess { seq ->
                seq.asTransactions().apply {
                    logActualToExpectedLists(
                        "Transactions' hashes from DB:",
                        map { it.hash.toHexString() },
                        "Transactions' hashes from test:",
                        transactions.map { it.hash.toHexString() }
                    )
                    assertThat(size).isEqualTo(
                        transactions.size
                    )
                    assertThat(this).containsExactly(
                        *transactions.toTypedArray()
                    )

                }
            }.failOnError()

        }

        @Test
        fun `loading transactions by Public Key`() {
            pw.getTransactionsFromAgent(
                chainId.tag, id.publicKey
            ).mapSuccess { seq ->
                seq.asTransactions().apply {
                    logActualToExpectedLists(
                        "Transactions' hashes from DB:",
                        map { it.hash.toHexString() },
                        "Transactions' hashes from test:",
                        transactions.map { it.hash.toHexString() }
                    )
                    assertThat(size).isEqualTo(
                        transactions.size
                    )
                    assertThat(this).containsOnly(
                        *transactions.toTypedArray()
                    )
                }
            }.failOnError()
        }

        @Test
        fun `loading transaction by hash`() {
            pw.getTransactionByHash(
                chainId.tag,
                transactions.elementAt(2).hash
            ).mapSuccess {
                assertThat((it as StorageAwareTransaction).transaction)
                    .isNotNull()
                    .isEqualTo(transactions.elementAt(2))
            }.failOnError()
        }
    }

    @AfterAll
    fun `close database`() {
        ledger.close()
    }

}