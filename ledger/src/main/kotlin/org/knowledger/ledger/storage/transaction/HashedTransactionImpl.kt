package org.knowledger.ledger.storage.transaction

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.knowledger.ledger.core.data.PhysicalData
import org.knowledger.ledger.core.hash.Hash
import org.knowledger.ledger.core.hash.Hasher
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.crypto.service.Identity
import org.knowledger.ledger.storage.HashUpdateable
import java.security.PrivateKey
import java.security.PublicKey

@Serializable
@SerialName("HashedTransaction")
internal data class HashedTransactionImpl(
    val signedTransaction: SignedTransactionImpl,
    @SerialName("hash")
    internal var _hash: Hash? = null
) : HashedTransaction,
    HashUpdateable,
    SignedTransaction by signedTransaction {
    @Transient
    private var cachedSize: Long? = null

    override val approximateSize: Long?
        get() = cachedSize

    override val hash: Hash
        get() = _hash ?: throw UninitializedPropertyAccessException("Hash was not initialized")

    constructor(
        privateKey: PrivateKey, publicKey: PublicKey,
        data: PhysicalData, hasher: Hashers, encoder: BinaryFormat
    ) : this(
        signedTransaction = SignedTransactionImpl(
            privateKey = privateKey,
            publicKey = publicKey,
            data = data, encoder = encoder
        )
    ) {
        updateHash(hasher, encoder)
    }

    constructor(
        publicKey: PublicKey, data: PhysicalData,
        signature: ByteArray, hash: Hash
    ) : this(
        signedTransaction = SignedTransactionImpl(
            publicKey = publicKey, data = data,
            signature = signature
        ), _hash = hash
    )

    constructor(
        identity: Identity, data: PhysicalData,
        hasher: Hashers, encoder: BinaryFormat
    ) : this(
        privateKey = identity.privateKey,
        publicKey = identity.publicKey,
        data = data, hasher = hasher,
        encoder = encoder
    )


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

    override fun updateHash(
        hasher: Hasher, encoder: BinaryFormat
    ) {
        val bytes = signedTransaction.serialize(encoder)
        _hash = hasher.applyHash(bytes)
        cachedSize = cachedSize ?: bytes.size.toLong() +
                _hash!!.bytes.size.toLong()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is HashedTransactionImpl) return false

        if (signedTransaction != other.signedTransaction) return false
        if (_hash != other._hash) return false

        return true
    }

    override fun hashCode(): Int {
        var result = signedTransaction.hashCode()
        result = 31 * result + (_hash?.hashCode() ?: 0)
        return result
    }


}