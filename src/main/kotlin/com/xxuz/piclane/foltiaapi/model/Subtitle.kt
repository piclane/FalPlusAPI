package com.xxuz.piclane.foltiaapi.model

import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.util.*

/**
 * 放送
 */
@Suppress("unused")
data class Subtitle(
    /** プライマリキー */
    val pId: Long,

    /** 番組ID (foltia_program.tid に連結) */
    val tId: Long,

    /** チャンネルID (foltia_station.stationid に連結) */
    val stationId: Long,

    /** 話数 */
    val countNo: Long?,

    /** サブタイトル */
    val subtitle: String?,

    /** 放送開始日時 */
    val startDateTime: LocalDateTime,

    /** 放送終了日時 */
    val endDateTime: LocalDateTime,

    /** 開始時刻オフセット (秒) */
    val startOffset: Long,

    /** 放映尺 (分) */
    val lengthMin: Long,

    /** TS のファイル名 */
    val m2pFilename: String?,

    /** SD 動画ファイル名 */
    val pspFilename: String?,

    /** EPG 録画の場合、登録したユーザID そうでない場合 NULL */
    val epgAddedBy: Long?,

    /** 最終更新日時 */
    val lastUpdate: OffsetDateTime?,

    /** ステータス */
    val fileStatus: FileStatus?,

    /** アスペクト比 */
    val aspect: Int?,

    /** トランスコード品質 */
    val encodeSetting: TranscodeQuality?,

    /** HD 動画ファイル名 */
    val mp4hd: String?,

    /** しょぼいカレンダーフラグ */
    val syobocalFlag: Set<SyobocalFlag>,

    /** しょぼいカレンダー修正回数 */
    val syobocalRev: Int,
) {
    /** 録画タイプ */
    val recordingType: RecordingType
        get() = when {
            tId > 0 -> RecordingType.Program
            tId == 0L -> RecordingType.Epg
            tId == -1L -> RecordingType.Keyword
            else -> throw IllegalArgumentException("Illegal tId: $tId")
        }

    /**
     * 動画ファイル名を取得します
     *
     * @param videoType 動画ファイルの種別
     */
    fun videoFilename(videoType: VideoType): String? =
        when(videoType) {
            VideoType.TS -> m2pFilename
            VideoType.SD -> pspFilename
            VideoType.HD -> mp4hd
        }

    /** 録画タイプ型 */
    enum class RecordingType {
        /** アニメ自動録画 */
        Program,

        /* EPG 録画 */
        Epg,

        /** キーワード録画 */
        Keyword;
    }

    /**
     * ステータス
     */
    enum class FileStatus(
        /** コード */
        val code: Int
    ) {
        /** 予約中(5分以上先) */
        RESERVING_LONG(10),

        /** 予約中(5分以内) */
        RESERVING_SHORT(20),

        /** 録画中 */
        RECORDING(30),

        /** TSSplit中 */
        REC_TS_SPLITTING(40),

        /** MPEG2録画終了 */
        RECEND(50),

        /** 静止画キャプチャ待 */
        WAITING_CAPTURE(55),

        /** 静止画キャプ中 */
        CAPTURE(60),

        /** 静止画キャプ終了 */
        CAPEND(70),

        /** サムネイル作成済み(.THM) */
        THM_CREATE(72),

        /** トラコン待 */
        WAITING_TRANSCODE(80),

        /** トラコン中:TSsplit */
        TRANSCODE_TS_SPLITTING(90),

        /** トラコン中:H264 */
        TRANSCODE_FFMPEG(100),

        /** トラコン中:WAVE */
        TRANSCODE_WAVE(110),

        /** トラコン中:AAC */
        TRANSCODE_AAC(120),

        /** トラコン中:MP4Box */
        TRANSCODE_MP4BOX(130),

        /** トラコン中:ATOM */
        TRANSCODE_ATOM(140),

        /** トラコン完了 */
        TRANSCODE_COMPLETE(150),

        /** HDトラコン待機中 */
        WAITING_HD_TRANSCODE(160),

        /** 全完了 */
        ALL_COMPLETE(200);

        companion object {
            /**
             * コードから FileStatus を取得します
             */
            fun codeOf(code: Int): Optional<FileStatus> =
                Optional.ofNullable(values().find { it.code == code })
        }
    }

    /**
     * トランスコード品質
     */
    enum class TranscodeQuality(
        val code: Int
    ) {
        /** 変換しない */
        NONE(0),

        /** SD のみ */
        SD(1),

        /** HD のみ */
        HD(2),

        /** SD + HD */
        BOTH(3);

        companion object {
            /**
             * コードから TranscodeQuality を取得します
             */
            fun codeOf(code: Int): Optional<TranscodeQuality> =
                Optional.ofNullable(values().find { it.code == code })
        }
    }

    /**
     * しょぼいカレンダーフラグ
     * https://docs.cal.syoboi.jp/spec/proginfo-flag/
     */
    enum class SyobocalFlag(
        val code: Int
    ) {
        /** 注 */
        Attention(1),

        /** 新番組 */
        New(2),

        /** 最終回 */
        End(4),

        /** 再放送 */
        Rerun(8);

        companion object {
            /**
             * コードから SyobocalFlag の集合を取得します
             */
            fun codesOf(code: Int): Set<SyobocalFlag> {
                val set = EnumSet.noneOf(SyobocalFlag::class.java)
                values().forEach {
                    if((it.code and code) > 0) {
                        set.add(it)
                    }
                }
                return set
            }
        }
    }
}
