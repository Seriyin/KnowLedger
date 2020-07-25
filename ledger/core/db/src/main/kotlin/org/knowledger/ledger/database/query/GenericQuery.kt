package org.knowledger.ledger.database.query

interface GenericQuery {
    val query: String
    val params: Map<String, Any>
}