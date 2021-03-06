package com.xxuz.piclane.foltiaapi.model

import java.util.*

/**
 * チャンネル
 */
data class Station(
        /** チャンネルID */
        val stationId: Long,

        /** 局名 */
        val stationName: String,

        /** 廃止 */
//        val stationRecCh: Long,

        /** 不明 */
        val stationCallSign: String?,

        /** 局の URL */
        val stationUri: String?,

        /** 廃止 */
//        val tunerType: String?,

        /** 廃止 */
//        val tunerCh: String?,

        /** 廃止 */
//        val device: String?,

        /** ontvcode */
        val ontvcode: String?,

        /** 物理チャンネル */
        val digitalCh: Long?,

        /** 種別 */
        val digitalStationBand: DigitalStationBand?,

        /** EPG 名 */
        val epgName: String?,

        /** 受信可否 */
        val receiving: Boolean,

        /** CM 検出閾値 */
        val cmEditDetectThreshold: CmEdit.DetectThreshold,
) {
    /** 種別 */
    enum class DigitalStationBand(
            /** コード */
            val code: Int
    ) {
        /** BS デジタル */
        BS(1),

        /** 110度 CS */
        CS(2),

        /** 地上波デジタル */
        TERRESTRIAL(0),

        /** IPサイマルラジオ */
        RADIO(3),

        /** 未定義 */
        UNDEFINED(10);

        companion object {
            /**
             * コードから DigitalStationBand を取得します
             */
            fun codeOf(code: Int): Optional<DigitalStationBand> =
                    Optional.ofNullable(values().find { it.code == code })
        }
    }
}
