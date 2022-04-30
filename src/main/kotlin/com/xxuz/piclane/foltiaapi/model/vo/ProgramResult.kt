package com.xxuz.piclane.foltiaapi.model.vo

import com.xxuz.piclane.foltiaapi.model.Program

/**
 * 番組取得結果
 */
data class ProgramResult(
    /** 検索の先頭からのオフセット */
    val offset: Int,

    /** 検索結果の最大取得件数  */
    val limit: Int,

    /** 任意のコンテキストデータ */
    val contextData: String?,

    /** 総行数 */
    val total: Int,

    /** ページのデータ */
    val data: List<Program>
)
