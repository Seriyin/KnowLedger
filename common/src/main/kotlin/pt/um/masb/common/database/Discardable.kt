package pt.um.masb.common.database

interface Discardable<out T : Any> {
    fun discard(): T
}