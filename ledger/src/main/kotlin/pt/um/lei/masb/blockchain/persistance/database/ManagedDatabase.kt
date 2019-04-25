package pt.um.lei.masb.blockchain.persistance.database

import com.orientechnologies.orient.core.db.OrientDB

interface ManagedDatabase {
    val instance: OrientDB

    fun newManagedSession(): ManagedSession
}