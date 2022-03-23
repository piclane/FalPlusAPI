package com.xxuz.piclane.foltiaapi.model.vo

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
)
