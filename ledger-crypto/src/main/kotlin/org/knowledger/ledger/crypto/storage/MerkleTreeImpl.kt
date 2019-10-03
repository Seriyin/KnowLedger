package org.knowledger.ledger.crypto.storage

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.knowledger.ledger.core.hash.Hash
import org.knowledger.ledger.core.hash.Hashing
import org.knowledger.ledger.core.misc.mapAndAdd
import org.knowledger.ledger.core.misc.mapToArray
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.crypto.serial.HashAlgorithmSerializer

@Serializable
@SerialName("MerkleTree")
data class MerkleTreeImpl(
    @SerialName("collapsedTree")
    internal val _collapsedTree: MutableList<Hash> = mutableListOf(),
    @SerialName("levelIndex")
    internal val _levelIndex: MutableList<Int> = mutableListOf(),
    @Serializable(with = HashAlgorithmSerializer::class)
    private var hasher: Hashers
) : MerkleTree {
    override fun serialize(encoder: BinaryFormat): ByteArray =
        encoder.dump(serializer(), this)

    override fun clone(): MerkleTree =
        copy()

    override val collapsedTree: List<Hash>
        get() = _collapsedTree
    override val levelIndex: List<Int>
        get() = _levelIndex


    constructor(
        hasher: Hashers,
        data: Array<out Hashing>
    ) : this(hasher = hasher) {
        rebuildMerkleTree(data)
    }

    constructor(
        hasher: Hashers,
        coinbase: Hashing,
        data: Array<out Hashing>
    ) : this(hasher = hasher) {
        rebuildMerkleTree(coinbase, data)
    }

    constructor(
        hasher: Hashers,
        collapsedTree: List<Hash>,
        levelIndex: List<Int>
    ) : this(
        collapsedTree.toMutableList(),
        levelIndex.toMutableList(),
        hasher
    )

    override fun hasTransaction(hash: Hash): Boolean {
        var res = false
        var i = _collapsedTree.size - 1

        //levelIndex[index] points to leftmost node at level index of the tree
        while (i >= _levelIndex[(_levelIndex.size - 1)]) {
            if (_collapsedTree[i] == hash) {
                res = true
                break
            }
            i -= 1
        }
        return res
    }

    override fun getTransactionId(hash: Hash): Int? {
        var res: Int? = null
        var i = _collapsedTree.size - 1

        //levelIndex[index] points to leftmost node at level index of the tree.
        while (i >= _levelIndex[_levelIndex.size - 1]) {
            if (_collapsedTree[i] == hash) {
                res = i
                break
            }
            i -= 1
        }
        return res
    }


    /**
     * Takes a [hash] to verify against the [MerkleTree]
     * and returns whether the transaction is present and
     * matched all the way up the [MerkleTree].
     */
    override fun verifyTransaction(hash: Hash): Boolean =
        getTransactionId(hash)?.let {
            loopUpVerification(
                it,
                hash,
                _levelIndex.size - 1
            )
        } ?: false


    @Suppress("NAME_SHADOWING")
    private fun loopUpVerification(
        index: Int,
        hash: Hash,
        level: Int
    ): Boolean {
        var res: Boolean = hash == _collapsedTree[index]
        var index = index
        var level = level
        var hash: Hash
        while (res && index != 0) {
            val delta = index - _levelIndex[level]

            //Is a left leaf
            hash = if (delta % 2 == 0) {

                //Is an edge case left leaf
                if (index + 1 == _collapsedTree.size ||
                    (level + 1 != _levelIndex.size &&
                            index + 1 == _levelIndex[level + 1])
                ) {
                    hasher.applyHash(
                        _collapsedTree[index] + _collapsedTree[index]
                    )
                } else {
                    hasher.applyHash(
                        _collapsedTree[index] + _collapsedTree[index + 1]
                    )
                }
            }

            //Is a right leaf
            else {
                hasher.applyHash(
                    _collapsedTree[index - 1] + _collapsedTree[index]
                )
            }
            level--

            //Index of parent is at the start of the last level
            // + the distance from start of this level / 2
            index = _levelIndex[level] + (delta / 2)
            res = hash == _collapsedTree[index]
        }
        return res
    }

    /**
     * Verifies entire [MerkleTree] against the transaction value.
     *
     * Takes the special [coinbase] transaction + the other [data]
     * transactions in the block and returns whether the entire
     * [MerkleTree] matches against the transaction [data] + [coinbase].
     */
    override fun verifyBlockTransactions(
        coinbase: Hashing,
        data: Array<out Hashing>
    ): Boolean =
        //Check if collapsedTree is empty.
        if (_collapsedTree.isNotEmpty() &&
            _collapsedTree.size - _levelIndex[_levelIndex.size - 1] == data.size + 1
        ) {
            if (checkAllTransactionsPresent(coinbase, data)) {
                if (_collapsedTree.size > 1) {
                    loopUpAllVerification(_levelIndex.size - 2)
                } else {
                    true
                }
            } else {
                false
            }
        } else {
            false
        }


    @Suppress("NAME_SHADOWING")
    private fun loopUpAllVerification(level: Int): Boolean {
        var res = true

        //2 fors evaluate to essentially checking level by level
        //starting at the second to last.
        //We already checked the last level immediately
        //against the value provided.
        var level = level
        while (level >= 0) {
            var i = _levelIndex[level]
            while (i < _levelIndex[level + 1]) {

                //Delta is level index difference + current index + difference
                //to current level index.
                //It checks out to exactly the left child leaf of any node.

                val delta = _levelIndex[level + 1] -
                        _levelIndex[level] +
                        i +
                        (i - _levelIndex[level])

                //Either the child is the last leaf in the next level, or is the last leaf.
                //Since we know delta points to left leafs both these conditions mean
                //edge case leafs.
                if ((level + 2 != _levelIndex.size && delta + 1 == _levelIndex[level + 2])
                    || delta + 1 == _collapsedTree.size
                ) {
                    if (_collapsedTree[i] !=
                        hasher.applyHash(
                            _collapsedTree[delta] + _collapsedTree[delta]
                        )
                    ) {
                        res = false
                        break
                    }
                }

                //Then it's a regular left leaf.
                else {
                    if (_collapsedTree[i] !=
                        hasher.applyHash(
                            _collapsedTree[delta] + _collapsedTree[delta + 1]
                        )
                    ) {
                        res = false
                        break
                    }
                }
                i += 1
            }
            level -= 1
        }
        return res
    }

    private fun checkAllTransactionsPresent(
        coinbase: Hashing,
        data: Array<out Hashing>
    ): Boolean {
        var i = _levelIndex[_levelIndex.size - 1] + 1
        var res = true
        val arr = data.map(Hashing::hash)
        if (_collapsedTree[i - 1] == coinbase.hash) {
            for (it in arr) {

                //There are at least as many transactions.
                //They match the ones in the merkle tree.
                if (i < _collapsedTree.size &&
                    it == _collapsedTree[i]
                ) {
                    i++
                } else {
                    res = false
                    break
                }
            }

            //There are less transactions in the provided block
            if (i != _collapsedTree.size) {
                res = false
            }
        } else {
            res = false
        }
        return res
    }

    override fun changeHasher(hasher: Hashers) {
        TODO("not implemented")
    }

    override fun buildDiff(diff: Array<out Hashing>, diffIndexes: Array<Int>) {
        TODO("not implemented")
    }

    override fun rebuildMerkleTree(data: Array<out Hashing>) {
        _collapsedTree.clear()
        _levelIndex.clear()
        val treeLayer = mutableListOf<Array<Hash>>()
        treeLayer.add(
            data.mapToArray(Hashing::hash)
        )
        buildLoop(treeLayer)
    }

    override fun rebuildMerkleTree(
        coinbase: Hashing, data: Array<out Hashing>
    ) {
        val treeLayer: MutableList<Array<Hash>> = mutableListOf()
        treeLayer.add(
            data.mapAndAdd(Hashing::hash, coinbase)
        )
        buildLoop(treeLayer)
    }

    private fun buildLoop(
        treeLayer: MutableList<Array<Hash>>
    ) {
        var i = 0
        //Next layer's node count for depth-1
        var count = (treeLayer[i].size / 2) + (treeLayer[i].size % 2)
        //While we're not at root yet:
        while (count > 1) {
            treeLayer.add(buildNewLayer(treeLayer[i], count))
            //Update to new count in next tree layer.
            count = (count / 2) + (count % 2)
            i += 1
        }
        when (treeLayer[i].size) {
            2 -> {
                _collapsedTree.add(
                    hasher.applyHash(
                        treeLayer[i][0] + treeLayer[i][1]
                    )
                )
                _levelIndex.add(0)
                treeLayer.reverse()
                count = 1
                for (s in treeLayer) {
                    _levelIndex.add(count)
                    _collapsedTree.addAll(s)
                    count += s.size
                }

            }
            //If the previous layer was already length 1,
            //that means we started at the root.
            1 -> {
                _collapsedTree.addAll(treeLayer[i].toList())
                _levelIndex.add(0)
            }
            else -> {
                throw InvalidMerkleException(treeLayer[i].size)
            }
        }
    }

    class InvalidMerkleException(val size: Int) : Throwable() {
        override val message: String?
            get() = "${super.message}: next to last was $size in size."
    }

    private fun buildNewLayer(
        previousTreeLayer: Array<Hash>,
        count: Int
    ): Array<Hash> {
        val treeLayer = Array(count) { Hash.emptyHash }
        var j = 0
        var i = 1
        //While we're inside the bounds of this layer, calculate two by two the hashId.
        while (i < previousTreeLayer.size) {
            treeLayer[j] = hasher.applyHash(
                previousTreeLayer[i - 1] + previousTreeLayer[i]
            )
            i += 2
            j += 1
        }
        //If we're still in the layer, there's one left, it's grouped and hashed with itself.
        if (j < treeLayer.size) {
            treeLayer[j] = hasher.applyHash(
                previousTreeLayer[previousTreeLayer.size - 1] +
                        previousTreeLayer[previousTreeLayer.size - 1]
            )
        }
        return treeLayer
    }

    override fun buildFromCoinbase(coinbase: Hashing) {
        var accumulate = coinbase.hash
        _collapsedTree[levelIndex.size - 1] = accumulate
        var i = levelIndex.size - 1
        var j = levelIndex[i]
        while (i > 0) {
            accumulate = hasher.applyHash(accumulate + _collapsedTree[j + 1])
            i -= 1
            j = levelIndex[i]
            _collapsedTree[j] = accumulate
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MerkleTreeImpl) return false
        if (collapsedTree != other.collapsedTree) return false
        if (levelIndex != other.levelIndex) return false

        return true
    }

    override fun hashCode(): Int {
        var result = hasher.hashCode()
        result = 31 * result + collapsedTree.hashCode()
        result = 31 * result + levelIndex.hashCode()
        return result
    }
}