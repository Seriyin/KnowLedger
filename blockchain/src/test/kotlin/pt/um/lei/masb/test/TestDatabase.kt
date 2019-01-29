package pt.um.lei.masb.test

import assertk.assertAll
import assertk.assertThat
import assertk.assertions.containsAll
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isTrue
import com.orientechnologies.orient.core.db.ODatabaseType
import com.orientechnologies.orient.core.record.OElement
import mu.KLogging
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import pt.um.lei.masb.blockchain.BlockChain
import pt.um.lei.masb.blockchain.Ident
import pt.um.lei.masb.blockchain.data.TemperatureData
import pt.um.lei.masb.blockchain.persistance.DatabaseMode
import pt.um.lei.masb.blockchain.persistance.ManagedDatabaseInfo
import pt.um.lei.masb.blockchain.persistance.ManagedSession
import pt.um.lei.masb.blockchain.persistance.ManagedSessionInfo
import pt.um.lei.masb.blockchain.persistance.PersistenceWrapper
import pt.um.lei.masb.blockchain.persistance.PluggableDatabase
import pt.um.lei.masb.blockchain.persistance.loaders.LoaderManager
import pt.um.lei.masb.blockchain.persistance.loaders.PreConfiguredLoaders
import pt.um.lei.masb.blockchain.print
import pt.um.lei.masb.blockchain.truncated
import pt.um.lei.masb.test.utils.logActualToExpectedLists
import pt.um.lei.masb.test.utils.makeXTransactions

class TestDatabase {
    val database: ManagedSession = PluggableDatabase(
        ManagedDatabaseInfo(
            modeOpen = DatabaseMode.MEMORY,
            path = "./test"
        )
    ).newManagedSession(
        ManagedSessionInfo(mode = ODatabaseType.MEMORY)
    )

    val pw = PersistenceWrapper(database)

    val session = pw.getInstanceSession()

    val ident = Ident.generateNewIdent()

    val testTransactions = makeXTransactions(ident, 10)

    val blockChain = BlockChain(pw, "test")

    val hash = blockChain.blockChainId.hash

    val trunc = hash.truncated()

    init {
        testTransactions.forEach {
            it.store(session).save<OElement>(
                "transaction${trunc.toLowerCase()}"
            )
        }
    }


    @Nested
    inner class TestClusters {
        @Test
        fun `Test created clusters`() {
            val clusterNames = database.session.clusterNames
            logger.info {
                """Clusters present in ${database.session.name}:
                    | ${clusterNames.joinToString(
                    """
                        ,
                    """.trimIndent()
                ) { it }}
                """.trimMargin()
                clusterNames
            }
            assertThat(
                clusterNames
            ).containsAll(
                "Transaction$trunc".toLowerCase(),
                "BlockChain$trunc".toLowerCase(),
                "SideChain$trunc".toLowerCase(),
                "TransactionOutput$trunc".toLowerCase(),
                "Coinbase$trunc".toLowerCase(),
                "Block$trunc".toLowerCase(),
                "BlockChainId$trunc".toLowerCase(),
                "BlockHeader$trunc".toLowerCase(),
                "PhysicalData$trunc".toLowerCase()
            )
        }

        @Test
        fun `Test cluster query`() {
            database.session.query(
                "select from cluster:transaction${
                trunc.toLowerCase()
                }"
            ).let { set ->
                val l = set.asSequence().toList()
                l.forEach { res ->
                    logger.info {
                        res.toJSON()
                    }
                }
                assertAll {
                    assertThat(l.size).isEqualTo(testTransactions.size)
                    l.map {
                        it.toElement().getProperty<ByteArray>(
                            "hashId"
                        )
                    }.forEachIndexed { i, hash ->
                        assertThat(hash.print()).isEqualTo(
                            testTransactions[i].hashId.print()
                        )
                    }
                }

            }
        }

        @Test
        fun `Test binary records`() {
            val binary = database.session.query(
                "select from cluster:transaction${
                trunc.toLowerCase()
                }"
            ).next().toElement().getProperty<ByteArray>(
                "hashId"
            )
            assertThat(binary).containsAll(
                *testTransactions[0].hashId
            )
        }

    }

    @Nested
    inner class TestQuerying {

        init {
            LoaderManager.registerLoaders(
                hash,
                PreConfiguredLoaders
            )
        }

        @Nested
        inner class TestTransactions {
            @Test
            fun `Test simple insertion`() {
                val present = database.session.browseClass(
                    "Transaction"
                ).toList()
                logger.info {
                    present[0].propertyNames.joinToString(
                        ", ",
                        """|
                       | Transaction properties:
                       |
                    """.trimMargin()
                    )
                }
                logActualToExpectedLists(
                    "Transactions' hashes from DB:",
                    present.map {
                        it.getProperty<ByteArray>("hashId").print()
                    },
                    "Transactions' hashes from test:",
                    testTransactions.map { it.hashId.print() },
                    logger
                )
                assertThat(
                    present.size
                ).isEqualTo(10)
            }

            @Test
            fun `Test loading transactions`() {
                val transactions = pw.getTransactionsByClass(
                    hash,
                    "Temperature"
                )
                logActualToExpectedLists(
                    "Transactions' hashes from DB:",
                    transactions.map { it.hashId.print() },
                    "Transactions' hashes from test:",
                    testTransactions.map { it.hashId.print() },
                    logger
                )
                assertAll {
                    assertThat(transactions.size).isEqualTo(testTransactions.size)
                    transactions.forEachIndexed { i, it ->
                        assertThat(it).isEqualTo(testTransactions[i])
                    }
                }
            }

            @Test
            fun `Test loading by timestamp`() {
                val transactions = pw.getTransactionsOrderedByTimestamp(
                    hash
                )
                val reversed = testTransactions.asReversed()
                logActualToExpectedLists(
                    "Transactions' hashes from DB:",
                    transactions.map { it.hashId.print() },
                    "Transactions' hashes from test:",
                    reversed.map { it.hashId.print() },
                    logger
                )
                assertAll {
                    assertThat(transactions.size).isEqualTo(
                        testTransactions.size
                    )
                    transactions.forEachIndexed { i, it ->
                        assertThat(it).isEqualTo(reversed[i])
                    }
                }
            }

            @Test
            fun `Test loading by Public Key`() {
                val transactions = pw.getTransactionsFromAgent(
                    hash,
                    ident.second
                )
                logActualToExpectedLists(
                    "Transactions' hashes from DB:",
                    transactions.map { it.hashId.print() },
                    "Transactions' hashes from test:",
                    testTransactions.map { it.hashId.print() },
                    logger
                )
                assertAll {
                    assertThat(transactions.size).isEqualTo(testTransactions.size)
                    transactions.forEachIndexed { i, it ->
                        assertThat(it).isEqualTo(testTransactions[i])
                    }
                }

            }

            @Test
            fun `Test loading by hash`() {
                val transaction = pw.getTransactionByHash(
                    hash,
                    testTransactions[2].hashId
                )
                logger.info {
                    transaction.toString()
                }
                assertThat(transaction)
                    .isNotNull()
                    .isEqualTo(testTransactions[2])
            }

        }

        @Nested
        inner class TestBlocks {
            val sidechain = blockChain.registerSideChainOf(
                TemperatureData::class.java,
                "Temperature"
            ).getSideChainOf(TemperatureData::class.java)

            @Test
            fun `Test simple insertion`() {
                assertThat(sidechain).isNotNull()
                val block = sidechain!!.newBlock()
                assertThat(block).isNotNull()
                assertThat(block!!.addTransaction(testTransactions[0]))
                    .isTrue()
                assertThat(block.data[0])
                    .isNotNull()
                    .isEqualTo(testTransactions[0])
            }
        }
    }


    companion object : KLogging()
}