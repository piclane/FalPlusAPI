package com.xxuz.piclane.foltiaapi.model.vo

import java.time.LocalDate

/**
 * 番組クエリ入力
 */
data class ProgramQueryInput(
    /** 番組開始時期の最小値 */
    val firstLightAfter: LocalDate?,

    /** 番組開始時期の最大値 */
    val firstLightBefore: LocalDate?,

    /** 番組タイトル (部分一致) */
    val titleContains: String?,

    /**
     * 録画が存在する番組を取得する場合 true
     * 録画が存在しない番組を取得する場合 false
     * すべての放送を取得する場合 null
     */
    val hasRecording: Boolean?,
)
