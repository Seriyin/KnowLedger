package org.knowledger.ledger.data


import com.squareup.moshi.JsonClass
import org.knowledger.ledger.core.data.SelfInterval
import org.knowledger.ledger.core.hash.Hash
import org.knowledger.ledger.core.hash.Hasher
import org.knowledger.ledger.core.misc.bytes
import org.knowledger.ledger.core.misc.encodeStringToUTF8
import org.knowledger.ledger.core.misc.flattenBytes
import java.io.InvalidClassException
import java.math.BigDecimal

/**
 *
 * Traffic Flow (https://developer.tomtom.com/online-traffic/online-traffic-documentation/flow-segment-value):
 * https://api.tomtom.com/traffic/services/4/flowSegmentData/absolute/10/json?key=<API_KEY>&point=41.503122,-8.480000
 *
 **/
@JsonClass(generateAdapter = true)
class TrafficFlowData(
    var functionalRoadClass: String,    //Indicates the road type
    var currentSpeed: Int,  //Current speed
    var freeFlowSpeed: Int, //Free flow speed expected under ideal conditions
    var currentTravelTime: Int, //Current travel time in sec based on fused real-time measurements
    var freeFlowTravelTime: Int,    //The travel time in sec which would be expected under ideal free flow conditions
    var confidence: Double,   //Measure of the quality of the provided travel time and speed
    var realtimeRatio: Double,   //The ratio between live and the historical value used to provide the response
    city: String = "TBD",
    citySeqNum: Int = 1
) : org.knowledger.ledger.data.AbstractTrafficIncident(
    city,
    citySeqNum
) {
    override fun calculateDiff(previous: SelfInterval): BigDecimal {
        return if (previous is TrafficFlowData) {
            calculateDiffTraffic(previous)
        } else {
            throw InvalidClassException(
                """SelfInterval supplied is:
                    |   ${previous.javaClass.name},
                    |   not ${this::class.java.name}
                """.trimMargin()
            )
        }
    }

    private fun calculateDiffTraffic(
        previous: TrafficFlowData
    ): BigDecimal {
        TODO()
    }

    override fun digest(c: Hasher): Hash =
        c.applyHash(
            flattenBytes(
                functionalRoadClass.encodeStringToUTF8(),
                currentSpeed.bytes(),
                freeFlowSpeed.bytes(),
                currentTravelTime.bytes(),
                freeFlowTravelTime.bytes(),
                confidence.bytes(),
                realtimeRatio.bytes(),
                cityName.encodeStringToUTF8(),
                citySeqNum.bytes()
            )
        )

    //Indicates the road type
    var functionalRoadClassDesc: String =
        when (this.functionalRoadClass) {
            "FRC0" -> "Motorway"
            "FRC1" -> "Major Road"
            "FRC2" -> "Other Major Road"
            "FRC3" -> "Secondary Road"
            "FRC4" -> "Local Connecting Road"
            "FRC5" -> "Local High Importance Road"
            "FRC6" -> "Local Road"
            else -> {
                "Unknown Road"
            }
        }


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TrafficFlowData) return false

        if (functionalRoadClass != other.functionalRoadClass) return false
        if (currentSpeed != other.currentSpeed) return false
        if (freeFlowSpeed != other.freeFlowSpeed) return false
        if (currentTravelTime != other.currentTravelTime) return false
        if (freeFlowTravelTime != other.freeFlowTravelTime) return false
        if (confidence != other.confidence) return false
        if (realtimeRatio != other.realtimeRatio) return false
        if (functionalRoadClassDesc != other.functionalRoadClassDesc) return false

        return true
    }

    override fun hashCode(): Int {
        var result = functionalRoadClass.hashCode()
        result = 31 * result + currentSpeed
        result = 31 * result + freeFlowSpeed
        result = 31 * result + currentTravelTime
        result = 31 * result + freeFlowTravelTime
        result = 31 * result + confidence.hashCode()
        result = 31 * result + realtimeRatio.hashCode()
        result = 31 * result + functionalRoadClassDesc.hashCode()
        return result
    }

    override fun toString(): String {
        return "TrafficFlowData(functionalRoadClass='$functionalRoadClass', currentSpeed=$currentSpeed, freeFlowSpeed=$freeFlowSpeed, currentTravelTime=$currentTravelTime, freeFlowTravelTime=$freeFlowTravelTime, confidence=$confidence, realtimeRatio=$realtimeRatio, functionalRoadClassDesc='$functionalRoadClassDesc')"
    }

    companion object {

        //Functional Road Class Constants
        const val MOTORWAY = 0
        const val MAJOR_ROAD = 1
        const val OTHER_MAJOR_ROAD = 2
        const val SECONDARY_ROAD = 3
        const val LOCAL_CONNECTING_ROAD = 4
        const val LOCAL_HIGH_IMP_ROAD = 5
        const val LOCAL_ROAD = 6
    }

}
