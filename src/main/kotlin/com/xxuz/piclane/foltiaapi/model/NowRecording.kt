package com.xxuz.piclane.foltiaapi.model

import java.time.OffsetDateTime

/**
 * 録画中
 */
data class NowRecording(
    /** 放送ID */
    val pId: Long,

    /** 録画ファイル名 */
    val recFilename: String,

    /** デバイス */
    val device: String?,

    /** 最終更新日時 */
    val lastUpdate: OffsetDateTime?
)
