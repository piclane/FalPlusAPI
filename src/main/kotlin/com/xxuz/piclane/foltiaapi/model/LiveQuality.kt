package com.xxuz.piclane.foltiaapi.model

import java.util.*

/**
 * ライブ品質
 */
enum class LiveQuality(
    /** コード */
    val code: Int,

    /** ライブ品質名 */
    val qualityName: String,
) {
    /**
     * Audio: bitRate=256Kbps samplingRate=44.1KHz
     * Video: bitRate=4500Kbps size=1280x720 frameRate=29.97fps gop=90
     */
    Q10(10, "4540k"),

    /**
     * Audio: bitRate=160Kbps samplingRate=44.1KHz
     * Video: bitRate=2500Kbps size=1280x720 frameRate=29.97fps gop=90
     */
    Q9(9, "2540k"),

    /**
     * Audio: bitRate=128Kbps samplingRate=44.1KHz
     * Video: bitRate=2000Kbps size=1024x576 frameRate=29.97fps gop=90
     */
    Q8(8, "2040k"),

    /**
     * Audio: bitRate=128Kbps samplingRate=44.1KHz
     * Video: bitRate=1800Kbps size=960x540 frameRate=29.97fps gop=90
     */
    Q7(7, "1840k"),

    /**
     * Audio: bitRate=128Kbps samplingRate=44.1KHz
     * Video: bitRate=1168Kbps size=640x360 frameRate=29.97fps gop=90
     */
    Q6(6, "1240k"),

    /**
     * Audio: bitRate=128Kbps samplingRate=32.0KHz
     * Video: bitRate=760Kbps size=640x360 frameRate=29.97fps gop=90
     */
    Q5(5, "840k"),

    /**
     * Audio: bitRate=128Kbps samplingRate=32.0KHz
     * Video: bitRate=600Kbps size=640x360 frameRate=29.97fps gop=90
     */
    Q4(4, "640k"),

    /**
     * Audio: bitRate=96Kbps samplingRate=32.0KHz
     * Video: bitRate=400Kbps size=400x224 frameRate=29.97fps gop=90
     */
    Q3(3, "440k"),

    /**
     * Audio: bitRate=48Kbps samplingRate=32.0KHz
     * Video: bitRate=200Kbps size=400x224 frameRate=14.985fps gop=45
     */
    Q2(2, "240k"),

    /**
     * Audio: bitRate=48Kbps samplingRate=32.0KHz
     * Video: bitRate=110Kbps size=400x224 frameRate=9.99fps gop=30
     */
    Q1(1, "150k");

    companion object {
        /**
         * コードから LiveQuality を取得します
         */
        fun codeOf(code: Int): Optional<LiveQuality> =
            Optional.ofNullable(values().find { it.code == code })

        /**
         * ライブ品質名から LiveQuality を取得します
         */
        fun qualityNameOf(qualityName: String): Optional<LiveQuality> =
            Optional.ofNullable(values().find { it.qualityName == qualityName })
    }
}
