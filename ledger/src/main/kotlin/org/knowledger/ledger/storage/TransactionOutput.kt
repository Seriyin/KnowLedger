package org.knowledger.ledger.storage

import com.squareup.moshi.JsonClass
import org.knowledger.ledger.core.Sizeable
import org.knowledger.ledger.core.config.LedgerConfiguration
import org.knowledger.ledger.core.data.Payout
import org.knowledger.ledger.core.hash.Hash
import org.knowledger.ledger.core.hash.Hash.Companion.emptyHash
import org.knowledger.ledger.core.hash.Hashable
import org.knowledger.ledger.core.hash.Hashed
import org.knowledger.ledger.core.hash.Hasher
import org.knowledger.ledger.core.misc.flattenBytes
import org.knowledger.ledger.core.storage.LedgerContract
import org.openjdk.jol.info.GraphLayout
import java.math.BigDecimal
import java.security.PublicKey

/**
 * TransactionOutput contains transaction hashes used
 * for calculating payout and the cumulative payout
 * for the publickey in the current containing
 * coinbase.
 */
@JsonClass(generateAdapter = true)
data class TransactionOutput(
    val publicKey: PublicKey,
    val prevCoinbase: Hash,
    internal var hash: Hash,
    var payout: Payout,
    var tx: MutableSet<Hash>,
    @Transient
    val hasher: Hasher = LedgerConfiguration.DEFAULT_CRYPTER
) : Sizeable, Hashed, Hashable,
    LedgerContract {

    override val hashId: Hash
        get() = hash

    override val approximateSize: Long
        get() = GraphLayout
            .parseInstance(this)
            .totalSize()


    constructor(
        publicKey: PublicKey,
        prevCoinbase: Hash,
        cumUTXO: Payout,
        newT: Hash,
        prev: Hash,
        hasher: Hasher
    ) : this(
        publicKey,
        prevCoinbase,
        emptyHash,
        Payout(BigDecimal.ZERO),
        mutableSetOf<Hash>(),
        hasher
    ) {
        addToPayout(cumUTXO, newT, prev)
    }


    fun addToPayout(
        payout: Payout,
        tx: Hash,
        prev: Hash
    ) {
        this.tx.add(prev + tx)
        this.payout = this.payout.add(payout)
        hash = digest(hasher)
    }

    override fun digest(c: Hasher): Hash =
        c.applyHash(
            flattenBytes(
                tx.sumBy { it.bytes.size },
                tx.asSequence().map { it.bytes },
                publicKey.encoded,
                prevCoinbase.bytes,
                payout.bytes
            )
        )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TransactionOutput) return false

        if (publicKey.encoded!!.contentEquals(
                other.publicKey.encoded
            )
        ) return false
        if (prevCoinbase.contentEquals(other.prevCoinbase)) return false
        if (hash.contentEquals(other.hash)) return false
        if (payout.contentEquals(other.payout)) return false
        if (tx.size != other.tx.size) return false
        if (tx.asSequence()
                .zip(other.tx.asSequence())
                .any { (tx1, tx2) ->
                    !tx1.contentEquals(tx2)
                }
        ) return false
        return true
    }

    override fun hashCode(): Int {
        var result = publicKey.hashCode()
        result = 31 * result + prevCoinbase.hashCode()
        result = 31 * result + hash.hashCode()
        result = 31 * result + payout.hashCode()
        result = 31 * result + tx.hashCode()
        return result
    }
}
