package pt.um.masb.ledger.config

import com.squareup.moshi.JsonClass
import pt.um.masb.common.hash.Hash
import pt.um.masb.common.hash.Hashed
import pt.um.masb.common.hash.Hasher
import pt.um.masb.common.misc.flattenBytes
import pt.um.masb.common.storage.LedgerContract

@JsonClass(generateAdapter = true)
data class ChainId internal constructor(
    val tag: String,
    val ledgerHash: Hash,
    override val hashId: Hash
) : Hashed, LedgerContract {

    internal constructor(
        tag: String,
        ledgerHash: Hash,
        hasher: Hasher
    ) : this(
        tag, ledgerHash,
        generateChainHandleHash(hasher, tag, ledgerHash)
    )


    override fun toString(): String = """
        |       ChainId {
        |           Tag: $tag
        |           Ledger: ${ledgerHash.print}
        |           Hash: ${hashId.print}
        |       }
    """.trimMargin()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LedgerId) return false

        if (tag != other.tag) return false
        if (!hashId.contentEquals(other.hashId)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = tag.hashCode()
        result = 31 * result + hashId.hashCode()
        return result
    }

    companion object {
        private fun generateChainHandleHash(
            hasher: Hasher,
            tag: String,
            ledgerHash: Hash
        ): Hash =
            hasher.applyHash(
                flattenBytes(
                    tag.toByteArray(),
                    ledgerHash.bytes
                )
            )
    }

}