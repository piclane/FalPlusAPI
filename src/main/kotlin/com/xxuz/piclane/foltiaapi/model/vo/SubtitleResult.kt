package com.xxuz.piclane.foltiaapi.model.vo

import com.xxuz.piclane.foltiaapi.model.Subtitle

/**
 * 放送取得結果
 */
data class SubtitleResult(
    /** ページインデックス */
    val page: Int,

    /** 総行数 */
    val total: Int,

    /** ページのデータ */
    val data: List<Subtitle>
)
