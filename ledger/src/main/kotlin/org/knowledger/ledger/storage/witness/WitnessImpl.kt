@file:UseSerializers(
    EncodedPublicKeyByteSerializer::class,
    HashSerializer::class,
    PayoutSerializer::class,
    TransactionOutputByteSerializer::class
)

package org.knowledger.ledger.storage.witness

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.knowledger.collections.MutableSortedList
import org.knowledger.collections.SortedList
import org.knowledger.collections.mutableSortedListOf
import org.knowledger.ledger.core.serial.HashSerializer
import org.knowledger.ledger.core.serial.PayoutSerializer
import org.knowledger.ledger.crypto.EncodedPublicKey
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.crypto.serial.EncodedPublicKeyByteSerializer
import org.knowledger.ledger.data.Payout
import org.knowledger.ledger.serial.MutableSortedListSerializer
import org.knowledger.ledger.serial.binary.TransactionOutputByteSerializer
import org.knowledger.ledger.storage.TransactionOutput
import org.knowledger.ledger.storage.transaction.output.transactionOutput

/**
 * [WitnessImpl] contains transaction hashes used
 * for calculating payout and the cumulative payout
 * for the publickey in the current containing
 * coinbase.
 */
@Serializable
internal data class WitnessImpl(
    override val publicKey: EncodedPublicKey,
    override val previousWitnessIndex: Int,
    override val previousCoinbase: Hash,
    private var _payout: Payout,
    @Serializable(with = MutableSortedListSerializer::class)
    private var _transactionOutputs: MutableSortedList<TransactionOutput>
) : Witness {
    override val payout: Payout
        get() = _payout

    override val transactionOutputs: SortedList<TransactionOutput>
        get() = _transactionOutputs

    internal constructor(
        publicKey: EncodedPublicKey, previousWitnessIndex: Int,
        previousCoinbase: Hash, payout: Payout,
        newIndex: Int, newTransaction: Hash,
        previousBlock: Hash, previousIndex: Int,
        previousTransaction: Hash
    ) : this(
        publicKey = publicKey,
        previousWitnessIndex = previousWitnessIndex,
        previousCoinbase = previousCoinbase,
        _payout = Payout.ZERO,
        _transactionOutputs = mutableSortedListOf()
    ) {
        addToPayout(
            payout = payout, newIndex = newIndex,
            newTransaction = newTransaction,
            previousBlock = previousBlock,
            previousIndex = previousIndex,
            previousTransaction = previousTransaction
        )
    }

    override fun clone(): WitnessImpl =
        copy()


    override fun addToPayout(
        payout: Payout, newIndex: Int, newTransaction: Hash,
        previousBlock: Hash, previousIndex: Int,
        previousTransaction: Hash
    ) {
        _transactionOutputs.add(
            transactionOutput(
                payout = payout, newIndex = newIndex,
                newTransaction = newTransaction,
                previousBlock = previousBlock,
                previousIndex = previousIndex,
                previousTransaction = previousTransaction
            )
        )
        _payout += payout
    }

    override fun serialize(encoder: BinaryFormat): ByteArray =
        encoder.dump(serializer(), this)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Witness) return false

        if (publicKey != other.publicKey) return false
        if (previousWitnessIndex != other.previousWitnessIndex) return false
        if (previousCoinbase != other.previousCoinbase) return false
        if (_payout != other.payout) return false
        if (_transactionOutputs != other.transactionOutputs) return false

        return true
    }

    override fun hashCode(): Int {
        var result = publicKey.hashCode()
        result = 31 * result + previousCoinbase.hashCode()
        result = 31 * result + previousWitnessIndex.hashCode()
        result = 31 * result + _payout.hashCode()
        result = 31 * result + _transactionOutputs.hashCode()
        return result
    }


}
