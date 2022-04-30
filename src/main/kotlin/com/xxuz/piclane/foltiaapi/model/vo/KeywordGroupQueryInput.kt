package com.xxuz.piclane.foltiaapi.model.vo

import com.xxuz.piclane.foltiaapi.model.VideoType

/**
 * キーワードグループクエリ入力
 */
data class KeywordGroupQueryInput(
    /**
     * 録画が存在するキーワードグループを取得する場合 true
     * 録画が存在しないキーワードグループを取得する場合 false
     * すべてのキーワードグループを取得する場合 null
     * videoTypes と同時に指定された場合の挙動は未定義です。
     */
    val hasRecording: Boolean?,

    /**
     * 動画ファイル種別
     * 指定された動画ファイル種別の内、いずれかの種別が存在する場合に、その放送が一致するとみなされます。
     * 空の Set が渡された場合および null の場合は、このフィルタは無視されます。
     * hasRecording と同時に指定された場合の挙動は未定義です。
     */
    val videoTypes: Set<VideoType>?,

    /** キーワード (部分一致) */
    val keywordContains: String?,
)
