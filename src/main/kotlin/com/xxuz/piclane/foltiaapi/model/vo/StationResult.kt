package com.xxuz.piclane.foltiaapi.model.vo

import com.xxuz.piclane.foltiaapi.model.Station

/**
 * チャンネル取得結果
 */
data class StationResult(
    /** 総行数 */
    val total: Int,

    /** ページのデータ */
    val data: List<Station>
)
