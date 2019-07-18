package org.knowledger.ledger.core.database.orient

import com.orientechnologies.orient.core.db.ODatabaseType
import com.orientechnologies.orient.core.metadata.schema.OType
import org.knowledger.ledger.core.database.DatabaseMode
import org.knowledger.ledger.core.database.DatabaseType
import org.knowledger.ledger.core.database.StorageType

fun StorageType.toOType(): OType =
    when (this) {
        StorageType.BOOLEAN -> OType.BOOLEAN
        StorageType.BYTE -> OType.BYTE
        StorageType.INTEGER -> OType.INTEGER
        StorageType.LONG -> OType.LONG
        StorageType.FLOAT -> OType.FLOAT
        StorageType.DOUBLE -> OType.DOUBLE
        StorageType.DECIMAL -> OType.DECIMAL
        StorageType.TIME -> OType.DATETIME
        StorageType.STRING -> OType.STRING
        StorageType.BYTES -> OType.BINARY
        StorageType.LINK -> OType.LINK
        StorageType.LIST -> OType.LINKLIST
        StorageType.SET -> OType.LINKSET
        StorageType.MAP -> OType.LINKMAP
        StorageType.LISTEMBEDDED -> OType.EMBEDDEDLIST
        StorageType.HASH -> OType.BINARY
        StorageType.PAYOUT -> OType.DECIMAL
        StorageType.DIFFICULTY -> OType.BINARY
    }

fun DatabaseType.toOType(): ODatabaseType =
    when (this) {
        DatabaseType.MEMORY -> ODatabaseType.MEMORY
        DatabaseType.LOCAL -> ODatabaseType.PLOCAL
    }

val DatabaseMode.mode: String
    get() = when (this) {
        DatabaseMode.EMBEDDED -> "plocal"
        DatabaseMode.REMOTE -> "remote"
        DatabaseMode.MEMORY -> "memory"
    }