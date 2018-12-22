package pt.um.lei.masb.blockchain.persistance.query

enum class Filters(val s: String) : Comparable<Filters> {
    WHERE("WHERE"),
    GROUP("GROUP BY"),
    ORDER("ORDER BY"),
    UNWIND("UNWIND"),
    SKIP("SKIP"),
    LIMIT("LIMIT"),
    FETCHPLAN("FETCHPLAN"),
    TIMEOUT("TIMEOUT"),
    PARALLEL("PARALLEL")
}