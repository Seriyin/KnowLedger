package org.knowledger.agent.behaviours.ledger

import jade.core.Agent
import jade.core.behaviours.SimpleBehaviour
import org.knowledger.base64.base64Encoded
import org.knowledger.ledger.mining.BlockState
import org.knowledger.ledger.mining.MiningState
import org.knowledger.ledger.service.handles.ChainHandle
import org.tinylog.kotlin.Logger


class Mining internal constructor(
    agent: Agent,
    private val sc: ChainHandle,
    private var state: BlockState.BlockReady
) : SimpleBehaviour(agent) {
    private var done: Boolean = false

    override fun action() {
        when (state.attemptMine()) {
            MiningState.Attempted -> {
                if (sc.checkAgainstTarget(state.hashId)) {
                    done = true
                    Logger.debug {
                        "Finished mining block no.${sc.currentBlockheight} -> ${state.merkleRoot}"
                    }
                } else {
                    Logger.debug {
                        "Block under threshold: ${state.hashId.base64Encoded()} < ${sc.currentDifficulty.base64Encoded()}"
                    }
                }
            }
            MiningState.Refresh -> {
                when (val refresh = sc.refreshHeader(state.merkleRoot)) {
                    is BlockState.BlockReady -> state = refresh
                    is BlockState.BlockFailure -> Logger.error("Block Failure")
                    is BlockState.BlockNotReady -> Logger.error("Block Not Ready")
                }
            }
        }
    }


    override fun done(): Boolean =
        done
}
