package com.xxuz.piclane.foltiaapi.model

/**
 * パケット識別子ごとの詳細
 */
data class DropInfoDetail(
    /** パケット識別子 */
    val pid: Int,

    /** パケット数 */
    val total: Long,

    /** ドロップしたパケット数 */
    val drop: Long,

    /** スクランブルされているパケット数 */
    val scrambling: Long,
)
