package org.knowledger.collections

/**
 * Sorted list implementation that delegates to internal mutable list.
 * Single adding is implemented via ([binarySearch] + addAt(index)).
 * Collections are added by single adding each element.
 * Collections are removed by single removing each element.
 */
class DelegatedSortedList<E> internal constructor(
    override val comparator: Comparator<E>,
    private val delegate: MutableList<E>,
) : MutableSortedList<E>, MutableList<E> by delegate {
    constructor(comparator: Comparator<E>) : this(comparator, mutableListOf())


    constructor(comparator: Comparator<E>, initial: Iterable<E>) : this(comparator) {
        delegate.addAll(initial)
    }

    override fun add(element: E): Boolean {
        val insertionIndex: Int = delegate.binarySearch(element, comparator)
        if (insertionIndex < 0) {
            //Invert the inverted insertion point
            delegate.add(-insertionIndex - 1, element)
            return true
        }
        return false
    }

    override fun add(index: Int, element: E) {
        val insertionIndex: Int = delegate.binarySearch(element, comparator)
        if (-insertionIndex - 1 == index) {
            //Invert the inverted insertion point
            delegate.add(index, element)
        }
    }

    override fun addAll(index: Int, elements: Collection<E>): Boolean =
        addAll(elements)

    override fun addAll(elements: Collection<E>): Boolean {
        elements.forEach(this::plusAssign)
        return true
    }

    override fun addWithIndex(element: E): Int {
        val insertionIndex: Int = delegate.binarySearch(element, comparator)
        if (insertionIndex < 0) {
            //Invert the inverted insertion point
            delegate.add(-insertionIndex - 1, element)
            return -insertionIndex - 1
        }
        return -1
    }

    override fun contains(element: E): Boolean =
        delegate.binarySearch(element, comparator) >= 0

    override operator fun plus(list: SortedList<E>): DelegatedSortedList<E> =
        DelegatedSortedList(comparator, delegate).apply { addAll(list) }

    override fun remove(element: E): Boolean {
        val removeIndex: Int = delegate.binarySearch(element, comparator)
        if (removeIndex >= 0) {
            delegate.removeAt(removeIndex)
            return true
        }
        return false
    }

    override fun removeWithIndex(element: E): Int {
        val removeIndex = delegate.binarySearch(element, comparator)
        if (removeIndex >= 0) {
            delegate.removeAt(removeIndex)
            return removeIndex
        }
        return -1
    }

    override fun removeAll(elements: Collection<E>): Boolean =
        elements.all(this::remove)

    override operator fun minus(list: SortedList<E>): DelegatedSortedList<E> =
        apply { removeAll(list) }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DelegatedSortedList<*>

        if (delegate != other.delegate) return false

        return true
    }

    override fun hashCode(): Int {
        return delegate.hashCode()
    }

    override fun replace(element: E): Boolean =
        delegate.binarySearch(element, comparator).let { index ->
            if (index >= 0) {
                delegate[index] = element
                return true
            }
            false
        }

    override fun replaceAt(index: Int, element: E): Boolean {
        if (index < 0) return false
        val lower = maxOf(0, index - 1)
        val upper = minOf(index + 1, size - 1)
        if (delegate[index] != element &&
            (lower == index || comparator.compare(delegate[lower], delegate[index]) < 0) &&
            (upper == index || comparator.compare(delegate[upper], delegate[index]) > 0)
        ) {
            delegate[index] = element
            return true
        }
        return false
    }
}