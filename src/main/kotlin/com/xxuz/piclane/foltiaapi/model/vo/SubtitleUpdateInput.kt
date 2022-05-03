package com.xxuz.piclane.foltiaapi.model.vo

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder
import com.xxuz.piclane.foltiaapi.model.Subtitle

/**
 * 放送更新入力
 */
@Suppress("unused")
@JsonDeserialize(builder = SubtitleUpdateInput.Builder::class)
data class SubtitleUpdateInput(
    /** プライマリキー */
    val pId: Long,

    /** サブタイトル */
    val subtitle: String?,
    val subtitleDefined: Boolean,

    /** ステータス */
    val fileStatus: Subtitle.FileStatus?,
    val fileStatusDefined: Boolean,

    /** トランスコード品質 */
    val encodeSetting: Subtitle.TranscodeQuality?,
    val encodeSettingDefined: Boolean,
) {
    @JsonPOJOBuilder(withPrefix = "set")
    class Builder {
        /** 放送ID */
        @JsonProperty("pId")
        var pId: Long? = null

        /** サブタイトル */
        var subtitle: String? = null
            set(value) {
                field = value
                subtitleDefined = true
            }

        var subtitleDefined: Boolean = false

        /** ステータス */
        var fileStatus: Subtitle.FileStatus? = null
            set(value) {
                field = value
                fileStatusDefined = true
            }

        var fileStatusDefined: Boolean = false

        /** トランスコード品質 */
        var encodeSetting: Subtitle.TranscodeQuality? = null
            set(value) {
                field = value
                encodeSettingDefined = true
            }

        var encodeSettingDefined: Boolean = false

        fun build() = SubtitleUpdateInput(
            pId ?: throw IllegalArgumentException("pId is undefined"),
            subtitle, subtitleDefined, fileStatus, fileStatusDefined, encodeSetting, encodeSettingDefined
        )
    }
}
