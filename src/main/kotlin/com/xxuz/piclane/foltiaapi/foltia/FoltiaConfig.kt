package com.xxuz.piclane.foltiaapi.foltia

import com.xxuz.piclane.foltiaapi.model.Subtitle
import com.xxuz.piclane.foltiaapi.model.VideoType
import java.io.File
import java.net.URI

/**
 * Foltia のコンフィグ
 */
data class FoltiaConfig(
    /** perl ディレクトリの親ディレクトリへのパス */
    val perlToolPath: File,

    /** php ディレクトリの親ディレクトリへのパス */
    val phpToolPath: File,

    /** 録画ディレクトリへのパス */
    val recFolderPath: File,

    /** HTTP メディアへのパス */
    val httpMediaMapPath: URI,

    /** バージョン */
    val firmwareVersion: String,
) {
    /** perl ファイルを取得します */
    fun perlPath(pl: String): File =
        File(this.perlToolPath, "perl${File.pathSeparator}${pl}")

    /** php ファイルを取得します */
    fun phpPath(php: String): File =
        File(this.phpToolPath, "php${File.pathSeparator}${php}")

    /**
     * 番組ディレクトリへのパスを取得します
     */
    private fun programPath(tId: Long) = recFolderPath.resolve("$tId.localized")

    /**
     * TS 動画へのパスを取得します
     */
    fun tsVideoPath(subtitle: Subtitle): File? =
        if(subtitle.m2pFilename == null)
            null
        else
            recFolderPath.resolve(subtitle.m2pFilename)

    /**
     * SD 動画へのパスを取得します
     */
    fun sdVideoPath(subtitle: Subtitle): File? =
        if(subtitle.pspFilename == null)
            null
        else
            programPath(subtitle.tId).resolve("mp4${File.separatorChar}${subtitle.pspFilename}")

    /**
     * HD 動画へのパスを取得します
     */
    fun hdVideoPath(subtitle: Subtitle): File? =
        if(subtitle.mp4hd == null)
            null
        else
            programPath(subtitle.tId).resolve("mp4${File.separatorChar}${subtitle.mp4hd}")

    /**
     * 動画へのパスを取得します
     */
    fun videoPath(subtitle: Subtitle, videoType: VideoType) =
        when(videoType) {
            VideoType.TS -> tsVideoPath(subtitle)
            VideoType.SD -> sdVideoPath(subtitle)
            VideoType.HD -> hdVideoPath(subtitle)
        }

    /**
     * サムネイルへのパスを取得します
     */
    fun thumbnailPath(subtitle: Subtitle): File? =
        if(subtitle.m2pFilename == null)
            null
        else {
            val thm = regexM2t.replace(subtitle.m2pFilename, "$1.THM")
            programPath(subtitle.tId).resolve("mp4${File.separatorChar}MAQ-${thm}")
        }

    /**
     * サムネイルの URI を取得します
     */
    fun thumbnailUri(subtitle: Subtitle): URI? =
        fileToUri(thumbnailPath(subtitle))

    /**
     * dropInfo ファイルへのパスを取得します
     */
    fun dropInfoPath(subtitle: Subtitle): File? =
        if(subtitle.m2pFilename == null)
            null
        else {
            val dropInfo = regexM2t.replace(subtitle.m2pFilename, "$1-dropinfo.txt")
            programPath(subtitle.tId).resolve("m2p${File.separatorChar}${dropInfo}")
        }

    /**
     * ファイルのパスから URI に変換します
     */
    private fun fileToUri(file: File?): URI? {
        if(file == null) {
            return null
        }

        if(file.startsWith(recFolderPath)) {
            return httpMediaMapPath.resolve(file.relativeTo(recFolderPath).toString())
        }
        return null
    }

    companion object {
        private val regexM2t = Regex("""^(.+)\.m2t$""", RegexOption.IGNORE_CASE)
    }
}
