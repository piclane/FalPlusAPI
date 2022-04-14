package com.xxuz.piclane.foltiaapi.model.vo

/**
 * キーワードグループクエリ入力
 */
data class KeywordGroupQueryInput(
    /**
     * 録画が存在するキーワードグループを取得する場合 true
     * 録画が存在しないキーワードグループを取得する場合 false
     * すべてのキーワードグループを取得する場合 null
     */
    val hasRecording: Boolean?,

    /** キーワード (部分一致) */
    val keywordContains: String?,
)
