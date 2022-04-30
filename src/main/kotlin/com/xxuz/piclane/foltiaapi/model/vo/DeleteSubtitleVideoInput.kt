package com.xxuz.piclane.foltiaapi.model.vo

import com.xxuz.piclane.foltiaapi.model.VideoType

/**
 * 放送動画削除入力
 */
data class DeleteSubtitleVideoInput(
    /** 放送ID */
    val pId: Long,

    /** 動画ファイルの種別 */
    val videoTypes: Set<VideoType>,
)
