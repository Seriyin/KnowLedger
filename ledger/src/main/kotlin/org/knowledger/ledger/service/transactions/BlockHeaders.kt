package org.knowledger.ledger.service.transactions

import org.knowledger.common.database.query.UnspecificQuery
import org.knowledger.common.hash.Hash
import org.knowledger.common.results.Outcome
import org.knowledger.ledger.service.results.LoadFailure
import org.knowledger.ledger.storage.BlockHeader
import org.knowledger.ledger.storage.adapters.BlockHeaderStorageAdapter


// ------------------------------
// Blockheader transactions.
//
// ------------------------------


internal fun PersistenceWrapper.getBlockHeaderByHash(
    chainHash: Hash,
    hash: Hash
): Outcome<BlockHeader, LoadFailure> =
    BlockHeaderStorageAdapter.let {
        queryUniqueResult(
            UnspecificQuery(
                """
                    SELECT 
                    FROM ${it.id} 
                    WHERE hashId = :hashId 
                        AND chainId.hashId = :chainHash
                    """.trimIndent(),
                mapOf(
                    "hashId" to hash.bytes,
                    "chainHash" to chainHash.bytes
                )
            ),
            it
        )
    }


internal fun PersistenceWrapper.getBlockHeaderByBlockHeight(
    chainHash: Hash,
    height: Long
): Outcome<BlockHeader, LoadFailure> =
    BlockHeaderStorageAdapter.let {
        queryUniqueResult(
            UnspecificQuery(
                """
                    SELECT 
                    FROM ${it.id} 
                    WHERE blockheight = :blockheight 
                        AND chainId.hashId = :chainHash
                """.trimIndent(),
                mapOf(
                    "blockheight" to height,
                    "chainHash" to chainHash.bytes
                )
            ),
            it
        )
    }


internal fun PersistenceWrapper.getBlockHeaderByPrevHeaderHash(
    chainHash: Hash,
    hash: Hash
): Outcome<BlockHeader, LoadFailure> =
    BlockHeaderStorageAdapter.let {
        queryUniqueResult(
            UnspecificQuery(
                """
                    SELECT 
                    FROM ${it.id} 
                    WHERE previousHash = :previousHash 
                        AND chainId.hashId = :chainHash
                """.trimIndent(),
                mapOf(
                    "previousHash" to hash.bytes,
                    "chainHash" to chainHash.bytes
                )
            ),
            it
        )

    }

internal fun PersistenceWrapper.getLatestBlockHeader(
    chainHash: Hash
): Outcome<BlockHeader, LoadFailure> =
    BlockHeaderStorageAdapter.let {
        queryUniqueResult(
            UnspecificQuery(
                """
                    SELECT 
                    FROM ${it.id} 
                    WHERE blockheight = max(blockheight) 
                        AND chainId.hashId = :chainHash
                """.trimIndent(),
                mapOf(
                    "chainHash" to chainHash.bytes
                )
            ),
            it
        )
    }