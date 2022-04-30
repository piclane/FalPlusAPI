package com.xxuz.piclane.foltiaapi.model

import java.time.LocalDateTime

/**
 * EPG
 */
data class Epg(
    /** EPG ID */
    val epgId: Long,

    /** 開始日時 */
    val startDateTime: LocalDateTime,

    /** 終了日時 */
    val endDateTime: LocalDateTime,

    /** onTvChannel (onTvCode) */
    val onTvChannel: String,

    /** 番組名 */
    val title: String,

    /** 説明 */
    val description: String,

    /** カテゴリ */
    val category: Category,
) {
    /**
     * カテゴリー
     */
    enum class Category(
        /** コード */
        val code: String
    )  {
        /** 情報 */
        INFORMATION("information"),

        /** 趣味・実用 */
        HOBBY("hobby"),

        /** 教育 */
        EDUCATION("education"),

        /** 音楽 */
        MUSIC("music"),

        /** 演劇 */
        STAGE("stage"),

        /** 映画 */
        CINEMA("cinema"),

        /** バラエティ */
        VARIETY("variety"),

        /** ニュース・報道 */
        NEWS("news"),

        /** ドラマ */
        DRAMA("drama"),

        /** ドキュメンタリー・教養 */
        DOCUMENTARY("documentary"),

        /** スポーツ */
        SPORTS("sports"),

        /** キッズ */
        KIDS("kids"),

        /** アニメ・特撮 */
        ANIME("anime"),

        /** その他 */
        ETC("etc");

        companion object {
            /**
             * コードから Category を取得します
             */
            fun codeOf(code: String): Category =
                values().find { it.code == code } ?: ETC
        }
    }
}
