package com.xxuz.piclane.foltiaapi.model.vo

import com.xxuz.piclane.foltiaapi.model.Direction
import com.xxuz.piclane.foltiaapi.model.Subtitle
import com.xxuz.piclane.foltiaapi.model.VideoType

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

    /**
     * 動画ファイル種別
     * 指定された動画ファイル種別の内、いずれかの種別が存在する場合に、その放送が一致するとみなされます。
     * 空の Set が渡された場合および null の場合は、このフィルタは無視されます。
     * hasRecording と同時に指定された場合の挙動は未定義です。
     */
    val videoTypes: Set<VideoType>?,

    /** キーワードグループID */
    val keywordGroupId: Long?,

    /** サブタイトル (部分一致) */
    val subtitleContains: String?,

    /** ソート方向 */
    val direction: Direction?,
)
