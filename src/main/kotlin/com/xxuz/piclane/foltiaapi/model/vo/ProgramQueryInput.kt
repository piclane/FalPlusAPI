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

    /** キーワード */
    val keyword: String?,
)
