package com.xxuz.piclane.foltiaapi.model

/**
 * ディスク情報
 */
data class DiskInfo(
    /** 総容量 (バイト) */
    val totalBytes: Long,

    /** 使用可能容量 (バイト) */
    val usableBytes: Long,
)
