package pt.um.masb.ledger.service.adapters

import pt.um.masb.common.database.NewInstanceSession
import pt.um.masb.common.database.StorageElement
import pt.um.masb.common.database.StorageType
import pt.um.masb.common.hash.Hash
import pt.um.masb.common.misc.stringToPrivateKey
import pt.um.masb.common.misc.stringToPublicKey
import pt.um.masb.common.results.Outcome
import pt.um.masb.ledger.results.tryOrLoadUnknownFailure
import pt.um.masb.ledger.service.Identity
import pt.um.masb.ledger.service.results.LoadFailure
import pt.um.masb.ledger.storage.adapters.LedgerStorageAdapter
import java.security.KeyPair

object IdentityStorageAdapter : LedgerStorageAdapter<Identity> {
    override val id: String
        get() = "Identity"

    override val properties: Map<String, StorageType>
        get() = mapOf(
            "id" to StorageType.STRING,
            "privateKey" to StorageType.BYTES,
            "publicKey" to StorageType.BYTES
        )

    override fun store(
        toStore: Identity, session: NewInstanceSession
    ): StorageElement =
        session.newInstance(id).apply {
            setStorageProperty("id", toStore.id)
            setStorageProperty(
                "privateKey", toStore.privateKey.encoded
            )
            setStorageProperty(
                "publicKey", toStore.publicKey.encoded
            )
        }

    override fun load(
        ledgerHash: Hash,
        element: StorageElement
    ): Outcome<Identity, LoadFailure> =
        tryOrLoadUnknownFailure {
            val keyPair = KeyPair(
                stringToPublicKey(
                    element.getStorageProperty("publicKey")
                ),
                stringToPrivateKey(
                    element.getStorageProperty("privateKey")
                )
            )
            Outcome.Ok(
                Identity(id, keyPair)
            )
        }
}