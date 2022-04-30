package com.xxuz.piclane.foltiaapi.foltia

import com.xxuz.piclane.foltiaapi.model.VideoType
import java.io.File

/**
 * 動画の削除に失敗した場合に発生する例外
 */
class DeleteVideoFailedException(
    /** 放送 ID */
    val pId: Long,

    /** 動画種別 */
    val videoType: VideoType,

    /** 動画ファイルへのパス */
    val path: File,

    /** 原因 */
    cause: Throwable,
): Exception("Failed to delete video: pId=${pId}, videoType=${videoType}, path=${path}", cause)
