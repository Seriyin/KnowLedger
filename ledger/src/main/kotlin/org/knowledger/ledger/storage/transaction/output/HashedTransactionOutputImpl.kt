package org.knowledger.ledger.storage.transaction.output

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.Transient
import kotlinx.serialization.cbor.Cbor
import org.knowledger.ledger.crypto.hash.Hash
import org.knowledger.ledger.crypto.hash.Hasher
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.crypto.hash.Hashers.Companion.DEFAULT_HASHER
import org.knowledger.ledger.data.Payout
import org.knowledger.ledger.storage.HashUpdateable
import java.security.PublicKey

internal data class HashedTransactionOutputImpl(
    val transactionOutput: TransactionOutputImpl,
    private var _hash: Hash? = null,
    @Transient
    var hasher: Hashers = DEFAULT_HASHER,
    @Transient
    var encoder: BinaryFormat = Cbor.plain
) : HashedTransactionOutput,
    HashUpdateable,
    TransactionOutput by transactionOutput {
    private var cachedSize: Long? = null

    override val approximateSize: Long
        get() = cachedSize ?: recalculateSize(hasher, encoder)

    override val hash
        get() = _hash ?: recalculateHash(hasher, encoder)


    internal constructor(
        publicKey: PublicKey, previousCoinbase: Hash,
        payout: Payout, newTransaction: Hash,
        previousTransaction: Hash, hasher: Hashers,
        encoder: BinaryFormat
    ) : this(
        transactionOutput = TransactionOutputImpl(
            publicKey = publicKey,
            previousCoinbase = previousCoinbase,
            payout = payout,
            newTransaction = newTransaction,
            previousTransaction = previousTransaction
        ),
        hasher = hasher,
        encoder = encoder
    ) {
        updateHash(hasher, encoder)
    }

    internal constructor(
        publicKey: PublicKey, previousCoinbase: Hash,
        payout: Payout, transactionSet: MutableSet<Hash>,
        hash: Hash, hasher: Hashers, encoder: BinaryFormat
    ) : this(
        transactionOutput = TransactionOutputImpl(
            publicKey = publicKey,
            previousCoinbase = previousCoinbase,
            _payout = payout,
            _transactionHashes = transactionSet
        ), _hash = hash, hasher = hasher, encoder = encoder
    )

    override fun clone(): HashedTransactionOutputImpl =
        copy(
            transactionOutput = transactionOutput.clone()
        )

    override fun updateHash(
        hasher: Hasher, encoder: BinaryFormat
    ) {
        val bytes = transactionOutput.serialize(encoder)
        _hash = hasher.applyHash(bytes)
        cachedSize = cachedSize ?: bytes.size.toLong() +
                _hash!!.bytes.size.toLong()
    }

    override fun recalculateSize(
        hasher: Hasher, encoder: BinaryFormat
    ): Long {
        updateHash(hasher, encoder)
        return cachedSize as Long
    }

    override fun recalculateHash(
        hasher: Hasher, encoder: BinaryFormat
    ): Hash {
        updateHash(hasher, encoder)
        return _hash as Hash
    }

    override fun addToPayout(
        payout: Payout, newTransaction: Hash, previousTransaction: Hash
    ) {
        transactionOutput.addToPayout(payout, newTransaction, previousTransaction)
        if (cachedSize != null) {
            cachedSize = (cachedSize as Long) +
                    newTransaction.bytes.size.toLong() +
                    previousTransaction.bytes.size.toLong()
        }
        updateHash(hasher, encoder)
    }

    override fun equals(
        other: Any?
    ): Boolean {
        if (this === other) return true
        if (other !is HashedTransactionOutput) return false

        if (transactionOutput != other) return false
        if (_hash != other.hash) return false

        return true
    }

    override fun hashCode(): Int {
        var result = transactionOutput.hashCode()
        result = 31 * result + (_hash?.hashCode() ?: 0)
        return result
    }


}