package org.knowledger.ledger.storage.config.chainid.factory

import kotlinx.serialization.BinaryFormat
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.storage.BlockParams
import org.knowledger.ledger.storage.ChainId
import org.knowledger.ledger.storage.CloningFactory
import org.knowledger.ledger.storage.CoinbaseParams
import org.knowledger.ledger.storage.Tag

interface ChainIdFactory : CloningFactory<ChainId> {
    fun create(
        hash: Hash, ledgerHash: Hash, tag: Tag,
        blockParams: BlockParams, coinbaseParams: CoinbaseParams
    ): ChainId

    fun create(
        ledgerHash: Hash, tag: Tag,
        hasher: Hashers, encoder: BinaryFormat,
        blockParams: BlockParams, coinbaseParams: CoinbaseParams
    ): ChainId
}