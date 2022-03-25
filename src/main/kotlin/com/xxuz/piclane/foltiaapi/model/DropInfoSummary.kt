package com.xxuz.piclane.foltiaapi.model

/**
 * ドロップ情報概要
 */
data class DropInfoSummary(
    /** すべてのパケット識別子ごとの詳細 */
    val details: List<DropInfoDetail>
) {
    /** 全パケット数 */
    val totalSum: Long
        get() = details.sumOf { it.total }

    /** 全ドロップしたパケット数 */
    val dropSum: Long
        get() = details.sumOf { it.drop }

    /** 全スクランブルされているパケット数 */
    val scramblingSum: Long
        get() = details.sumOf { it.scrambling }
}
