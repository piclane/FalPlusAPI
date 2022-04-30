package com.xxuz.piclane.foltiaapi.model.vo

import com.xxuz.piclane.foltiaapi.model.Station

/**
 * チャンネルクエリ入力
 */
data class StationQueryInput(
    /**
     * 受信可能なチャンネルのみを取得する場合 true
     * 受信不能なチャンネルのみを取得する場合 false
     * すべてのチャンネルを取得する場合 null
     */
    val receivableStation: Boolean?,

    /**
     * チャンネル種別
     */
    val digitalStationBands: Set<Station.DigitalStationBand>?,
)
