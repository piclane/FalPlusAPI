package com.xxuz.piclane.foltiaapi.model.vo

import com.xxuz.piclane.foltiaapi.model.VideoType
import java.time.LocalDate

/**
 * 番組クエリ入力
 */
data class ProgramQueryInput(
    /** 番組開始時期の最小値 */
    val firstLightAfter: LocalDate?,

    /** 番組開始時期の最大値 */
    val firstLightBefore: LocalDate?,

    /** 番組タイトル (部分一致) */
    val titleContains: String?,

    /**
     * 録画が存在する番組を取得する場合 true
     * 録画が存在しない番組を取得する場合 false
     * すべての放送を取得する場合 null
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
)
