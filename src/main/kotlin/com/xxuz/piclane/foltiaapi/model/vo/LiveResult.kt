package com.xxuz.piclane.foltiaapi.model.vo

import java.net.URI
import java.time.Duration

data class LiveResult(
    /** ライブID */
    val liveId: String,

    /** m3u8 ファイルへの URI */
    val m3u8Uri: URI,

    /** 推奨バッファ時間 */
    val preferredBufferTime: Duration,
)
