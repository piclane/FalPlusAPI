package com.xxuz.piclane.foltiaapi.model.vo

import com.xxuz.piclane.foltiaapi.model.Subtitle

/**
 * 放送取得結果
 */
data class SubtitleResult(
    /** 検索の先頭からのオフセット */
    val offset: Int,

    /** 検索結果の最大取得件数  */
    val limit: Int,

    /** 総行数 */
    val total: Int,

    /** ページのデータ */
    val data: List<Subtitle>
)
