package com.xxuz.piclane.foltiaapi.model.vo

import com.xxuz.piclane.foltiaapi.model.Subtitle

/**
 * 放送クエリ入力
 */
data class SubtitleQueryInput(
    /** 番組ID */
    val tId: Int?,

    /** 録画タイプ */
    val recordingType: Subtitle.RecordingType?,

    /**
     * 受信可能なチャンネルの放送のみを取得する場合 true
     * 受信不能なチャンネルの放送のみを取得する場合 false
     * すべての放送を取得する場合 null
     */
    val receivableStation: Boolean?,

    /**
     * 録画が存在する放送を取得する場合 true
     * 録画が存在しない放送を取得する場合 false
     * すべての放送を取得する場合 null
     */
    val hasRecording: Boolean?,

    /** キーワード */
    val keyword: String?,
)
