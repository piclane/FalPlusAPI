package com.xxuz.piclane.foltiaapi.model.vo

import com.xxuz.piclane.foltiaapi.model.Subtitle

/**
 * 放送クエリ入力
 */
data class SubtitleQueryInput(
    /** 番組ID */
    val tId: Int?,

    /** 録画タイプ */
    val recordingTypes: Set<Subtitle.RecordingType>?,

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

    /**
     * hasRecording=true の場合に、録画中の放送を「録画が存在する」とみなす場合 true
     * そうでない場合 false もしくは null
     */
    val nowRecording: Boolean?,

    /** キーワードグループID */
    val keywordGroupId: Long?,

    /** サブタイトル (部分一致) */
    val subtitleContains: String?,
)
