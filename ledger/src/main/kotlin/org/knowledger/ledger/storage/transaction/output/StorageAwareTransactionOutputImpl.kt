package org.knowledger.ledger.storage.transaction.output

import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.StorageID
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.service.results.UpdateFailure
import org.knowledger.ledger.storage.StoragePairs

internal data class StorageAwareTransactionOutputImpl(
    override val transactionOutput: TransactionOutput
) : TransactionOutput by transactionOutput,
    StorageAwareTransactionOutput {

    override var id: StorageID? = null
    override val invalidated: Array<StoragePairs<*>> = emptyArray()

    override fun update(
        session: ManagedSession
    ): Outcome<StorageID, UpdateFailure> =
        Outcome.Ok(id!!)

    override fun equals(other: Any?): Boolean =
        transactionOutput == other

    override fun hashCode(): Int =
        transactionOutput.hashCode()
}