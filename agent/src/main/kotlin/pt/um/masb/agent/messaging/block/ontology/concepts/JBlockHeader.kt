package pt.um.masb.agent.messaging.block.ontology.concepts

import jade.content.Concept
import pt.um.masb.ledger.config.BlockParams

data class JBlockHeader(
    var blid: String,
    var difficulty: String,
    var blockheight: Long,
    var hash: String,
    var merkleRoot: String,
    var previousHash: String,
    var params: BlockParams,
    var timeStamp: String,
    var nonce: Long
) : Concept
