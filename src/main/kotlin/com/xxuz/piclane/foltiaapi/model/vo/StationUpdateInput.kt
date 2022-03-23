package com.xxuz.piclane.foltiaapi.model.vo

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder
import com.xxuz.piclane.foltiaapi.model.Station

/**
 * チャンネル更新入力
 */
@Suppress("unused")
@JsonDeserialize(builder = StationUpdateInput.Builder::class)
data class StationUpdateInput(
    /** チャンネルID */
    val stationId: Long,

    /** 局名 */
    val stationName: String?,
    val stationNameDefined: Boolean,

    /** ontvcode */
    val ontvcode: String?,
    val ontvcodeDefined: Boolean,

    /** 物理チャンネル */
    val digitalCh: Long?,
    val digitalChDefined: Boolean,

    /** 受信可否 */
    val receiving: Boolean?,
    val receivingDefined: Boolean,

    /** CM 検出閾値 */
    val cmEditDetectThreshold: Station.CmEditDetectThreshold?,
    val cmEditDetectThresholdDefined: Boolean,
) {
    @JsonPOJOBuilder(withPrefix = "set")
    class Builder {
        /** チャンネルID */
        var stationId: Long? = null

        /** 局名 */
        var stationName: String? = null
            set(value) {
                field = value
                stationNameDefined = true
            }
        var stationNameDefined: Boolean = false

        /** ontvcode */
        var ontvcode: String? = null
            set(value) {
                field = value
                ontvcodeDefined = true
            }
        var ontvcodeDefined: Boolean = false

        /** 物理チャンネル */
        var digitalCh: Long? = null
            set(value) {
                field = value
                digitalChDefined = true
            }
        var digitalChDefined: Boolean = false

        /** 受信可否 */
        var receiving: Boolean? = null
            set(value) {
                field = value
                receivingDefined = true
            }
        var receivingDefined: Boolean = false

        /** CM 検出閾値 */
        var cmEditDetectThreshold: Station.CmEditDetectThreshold? = null
            set(value) {
                field = value
                cmEditDetectThresholdDefined = true
            }
        var cmEditDetectThresholdDefined: Boolean = false

        fun build() = StationUpdateInput(
            stationId ?: throw IllegalArgumentException("stationId is undefined"),
            stationName, stationNameDefined, ontvcode, ontvcodeDefined, digitalCh, digitalChDefined,
            receiving, receivingDefined, cmEditDetectThreshold, cmEditDetectThresholdDefined
        )
    }
}
