package pt.um.masb.common.data

import pt.um.masb.common.Sizeable
import pt.um.masb.common.hash.Hashable
import java.io.Serializable

interface BlockChainData : SelfInterval,
                           DataCategory,
                           Sizeable,
                           Hashable,
                           Serializable