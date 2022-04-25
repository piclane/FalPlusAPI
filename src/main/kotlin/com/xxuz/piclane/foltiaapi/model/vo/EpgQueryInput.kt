package com.xxuz.piclane.foltiaapi.model.vo

import java.time.LocalDateTime

/**
 * EPG クエリ入力
 */
data class EpgQueryInput(
    /** 指定された日時より前に始まる */
    val startBefore: LocalDateTime?,

    /** 指定された日時より後に始まる */
    val startAfter: LocalDateTime?,

    /** 指定された日時より前に終わる */
    val endBefore: LocalDateTime?,

    /** 指定された日時より後に終わる */
    val endAfter: LocalDateTime?,
) {
    companion object {
        /**
         * 現在の放送を取得するクエリ
         */
        fun now() =
            LocalDateTime.now()
                .let {
                    EpgQueryInput(
                        startBefore = it,
                        startAfter = null,
                        endBefore = null,
                        endAfter = it,
                    )
                }
    }
}
