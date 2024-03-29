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

    /** ライブのバッファ時間 (秒) */
    val liveBufferTime: Int,

    /** 追いかけ再生の場合のバッファ時間 (秒) */
    val chasingPlaybackBufferTime: Int,

    /** バージョン */
    val firmwareVersion: String,
) {
    /**
     * ライブディレクトリへのパスを取得します
     */
    val livePath: File
        get() = recFolderPath.resolve("live")

    /** perl ファイルを取得します */
    fun perlPath(pl: String): File =
        File(this.perlToolPath, "perl${File.separator}${pl}")

    /** php ファイルを取得します */
    fun phpPath(php: String): File =
        File(this.phpToolPath, "php${File.separator}${php}")

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
     * TS 動画の URI を取得します
     */
    fun tsVideoUri(subtitle: Subtitle): URI? =
        fileToUri(tsVideoPath(subtitle))

    /**
     * SD 動画へのパスを取得します
     */
    fun sdVideoPath(subtitle: Subtitle): File? =
        if(subtitle.pspFilename == null)
            null
        else
            programPath(subtitle.tId).resolve("mp4${File.separatorChar}${subtitle.pspFilename}")

    /**
     * SD 動画の URI を取得します
     */
    fun sdVideoUri(subtitle: Subtitle): URI? =
        fileToUri(sdVideoPath(subtitle))

    /**
     * HD 動画へのパスを取得します
     */
    fun hdVideoPath(subtitle: Subtitle): File? =
        if(subtitle.mp4hd == null)
            null
        else
            programPath(subtitle.tId).resolve("mp4${File.separatorChar}${subtitle.mp4hd}")

    /**
     * HD 動画の URI を取得します
     */
    fun hdVideoUri(subtitle: Subtitle): URI? =
        fileToUri(hdVideoPath(subtitle))

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
     *
     * @param nullIfAbsent
     *      サムネイルのファイルが存在しない場合は null を返す場合は true
     *      サムネイルのファイルの存在に関わらずパスを取得する場合は false
     */
    fun thumbnailPath(subtitle: Subtitle, nullIfAbsent: Boolean = true): File? =
        subtitle.someFilename?.let { someFilename ->
            val thm = regexM2t.replace(someFilename, "$1.THM")
            val file = programPath(subtitle.tId).resolve("mp4${File.separatorChar}MAQ-${thm}")
            if(!nullIfAbsent || file.exists()) file else null
        }

    /**
     * サムネイルの URI を取得します
     *
     * @param nullIfAbsent
     *      サムネイルのファイルが存在しない場合は null を返す場合は true
     *      サムネイルのファイルの存在に関わらずパスを取得する場合は false
     */
    fun thumbnailUri(subtitle: Subtitle, nullIfAbsent: Boolean = true): URI? =
        fileToUri(thumbnailPath(subtitle, nullIfAbsent))

    /**
     * 動画全体のサムネイルへのパスを取得します
     */
    fun thumbnailPaths(subtitle: Subtitle): List<File>? =
        subtitle.someFilename?.let { someFilename ->
            val dirName = regexM2t.replace(someFilename, "$1")
            val dir = programPath(subtitle.tId).resolve("img${File.separatorChar}${dirName}")
            if(dir.isDirectory) {
                dir.listFiles { _, filename ->  filename.endsWith(".jpg") }?.asList()
            } else {
                null
            }
        }

    /**
     * 動画全体のサムネイルへの URI を取得します
     */
    fun thumbnailUris(subtitle: Subtitle): List<URI>? =
        thumbnailPaths(subtitle)?.mapNotNull { fileToUri(it) }

    /**
     * dropInfo ファイルへのパスを取得します
     */
    fun dropInfoPath(subtitle: Subtitle): File? =
        subtitle.someFilename?.let { someFilename ->
            val dropInfo = regexM2t.replace(someFilename, "$1-dropinfo.txt")
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
        private val regexM2t = Regex("""^(?:MHD-|MAQ-)?(.+)\.(m2t|mp4)$""", RegexOption.IGNORE_CASE)

        /** デフォルトのバッファ時間 (秒) */
        const val defaultBufferTime = 30
    }
}

/**
 * 何らかのファイル名を取得します
 */
private val Subtitle.someFilename: String?
    get() = this.m2pFilename ?: (this.mp4hd ?: this.pspFilename)
