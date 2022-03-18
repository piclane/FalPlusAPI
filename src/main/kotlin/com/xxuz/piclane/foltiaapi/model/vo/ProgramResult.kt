package com.xxuz.piclane.foltiaapi.model.vo

import com.xxuz.piclane.foltiaapi.model.Program

/**
 * 番組取得結果
 */
data class ProgramResult(
    /** ページインデックス */
    val page: Int,

    /** 総行数 */
    val total: Int,

    /** ページのデータ */
    val data: List<Program>
)
