package org.knowledger.ledger.database.adapters

/**
 * Thrown in the face of an attempt to query for
 * a storage adapter for a type that has not been
 * registered in any ledger.
 */
class StorageAdapterNotRegistered : Exception()
