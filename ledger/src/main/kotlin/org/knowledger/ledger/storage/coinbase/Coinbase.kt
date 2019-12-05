package org.knowledger.ledger.storage.coinbase

import org.knowledger.ledger.config.CoinbaseParams
import org.knowledger.ledger.data.DataFormula
import org.knowledger.ledger.data.Difficulty
import org.knowledger.ledger.data.Payout
import org.knowledger.ledger.serial.HashSerializable
import org.knowledger.ledger.storage.LedgerContract
import org.knowledger.ledger.storage.TransactionOutput

/**
 * The coinbase transaction. Pays out to contributors to
 * the ledger.
 *
 * The coinbase will be continually updated to reflect
 * changes to the block.
 */
interface Coinbase : Cloneable,
                     HashSerializable,
                     LedgerContract {

    val transactionOutputs: Set<TransactionOutput>
    var payout: Payout
    // Difficulty is fixed at block generation time.
    val difficulty: Difficulty
    var blockheight: Long
    var extraNonce: Long
    val formula: DataFormula
    val coinbaseParams: CoinbaseParams

    public override fun clone(): Coinbase
}

