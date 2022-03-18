package com.xxuz.piclane.foltiaapi.model.vo

import com.xxuz.piclane.foltiaapi.model.VideoType

/**
 * 放送動画アップロード入力
 */
data class UploadSubtitleVideoInput(
    /** 放送ID */
    val pId: Long,

    /** 動画ファイルの種別 */
    val videoType: VideoType,
)
