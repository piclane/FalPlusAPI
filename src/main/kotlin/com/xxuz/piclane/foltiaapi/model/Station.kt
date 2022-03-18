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
        val cmEditDetectThreshold: CmEditDetectThreshold?,
) {
    /** 種別 */
    enum class DigitalStationBand(
            /** コード */
            val code: Int
    ) {
        /** BS デジタル */
        BS(1),

        /** CS デジタル */
        CS(2),

        /** 地上波デジタル */
        TERRESTRIAL(0),

        /** ラジオ */
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

    /** CM 検出閾値 */
    enum class CmEditDetectThreshold(
            /** コード */
            val code: Int
    ) {
        /** オフ (無効) */
        OFF(0),

        /** 弱 (CM判定が緩くなり本編・CMがより多く残りやすい) */
        LOW(1),

        /** 中 */
        MEDIUM(2),

        /** 強 (CM判定が厳しくなり本編・CMがより多くカットされやすい)*/
        HIGH(3);

        companion object {
            /**
             * コードから CmEditDetectThreshold を取得します
             */
            fun codeOf(code: Int): Optional<CmEditDetectThreshold> =
                    Optional.ofNullable(values().find { it.code == code })
        }
    }
}
