package com.xxuz.piclane.foltiaapi.model

import java.time.LocalDate

/**
 * 番組
 */
data class Program(
    /** プライマリキー */
    val tId: Long,

    /** タイトル */
    val title: String,

    /* 廃止 */
//        val startweektype: String?,

    /** 廃止 */
//        val starttime: String?,

    /** 廃止 */
//        val lengthmin: String?,

    /** 番組開始 */
    val firstLight: LocalDate?,

    /** 廃止 */
//        val officialuri: String?,

    /** アスペクト比 */
    val aspect: Long?,

    /** 不要 */
//        val psp: Long?,

    /** 廃止 */
//        val transfer: String?,

    /** 廃止 */
//        val pspdirname: String?,

    /** タイトル (短縮) */
    val shorttitle: String,

    /** タイトル (読み) */
    val titleyomi: String,

    /** タイトル (英語) */
    val titleen: String,
) {
    companion object {
        /** キーワード録画の TID */
        const val KEYWORD_TID = -1L

        /** EPG 録画の TID */
        const val EPG_TID = 0L
    }
}
