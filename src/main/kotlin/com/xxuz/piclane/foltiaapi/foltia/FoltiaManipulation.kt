package com.xxuz.piclane.foltiaapi.foltia

import com.xxuz.piclane.foltiaapi.dao.LiveInfoDao
import com.xxuz.piclane.foltiaapi.dao.NowRecordingDao
import com.xxuz.piclane.foltiaapi.dao.StationDao
import com.xxuz.piclane.foltiaapi.dao.SubtitleDao
import com.xxuz.piclane.foltiaapi.model.*
import com.xxuz.piclane.foltiaapi.model.vo.DeleteSubtitleVideoInput
import com.xxuz.piclane.foltiaapi.model.vo.LiveResult
import com.xxuz.piclane.foltiaapi.model.vo.UploadSubtitleVideoInput
import com.xxuz.piclane.foltiaapi.util.StreamUtil
import com.xxuz.piclane.foltiaapi.util.pipeLog
import com.xxuz.piclane.foltiaapi.util.readFirstLine
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.time.Duration
import java.time.format.DateTimeFormatter
import java.util.concurrent.atomic.AtomicReference
import javax.annotation.PreDestroy
import javax.servlet.http.Part
import kotlin.streams.asSequence

@Repository
class FoltiaManipulation(
    @Autowired
    private val config: FoltiaConfig,

    @Autowired
    private val makeDlnaStructure: MakeDlnaStructure,

    @Autowired
    private val subtitleDao: SubtitleDao,

    @Autowired
    private val stationDao: StationDao,

    @Autowired
    private val nowRecordingDao: NowRecordingDao,

    @Autowired
    private val liveInfoDao: LiveInfoDao,

    @Autowired
    private val txMgr: PlatformTransactionManager,
) {
    private val logger = LoggerFactory.getLogger(FoltiaManipulation::class.java)

    /**
     * 終了
     */
    @PreDestroy
    fun shutdown() {
        stopLiveAll()
    }

    /**
     * トランスコードを開始します
     */
    fun startTranscode() {
        val transcodePl = config.perlPath("ipodtranscode.pl")
        ProcessBuilder()
            .directory(config.perlToolPath)
            .command(transcodePl.absolutePath)
            .redirectError(ProcessBuilder.Redirect.DISCARD)
            .redirectOutput(ProcessBuilder.Redirect.DISCARD)
            .start()
    }

    /** ファイル名向け日付フォーマッター */
    private val dtfFilename = DateTimeFormatter.ofPattern("yyyyMMdd-HHmm")

    /**
     * 放送のファイル名を生成します
     */
    private fun buildVideoFilename(subtitle: Subtitle, videoType: VideoType): String {
        val station = stationDao.get(subtitle.stationId) ?: throw IllegalArgumentException("stationId ${subtitle.stationId} is invalid.")
        val base = "${subtitle.tId}-${subtitle.countNo}-${dtfFilename.format(subtitle.startDateTime)}-${station.digitalCh}"
        return when(videoType) {
            VideoType.TS -> "$base.m2t"
            VideoType.SD -> "MAQ-$base.MP4"
            VideoType.HD -> "MAQ-$base.MP4"
        }
    }

    /**
     * 放送の動画をアップロードします
     */
    fun uploadSubtitleVideo(input: UploadSubtitleVideoInput, video: Part): Subtitle {
        val tx = TransactionTemplate(txMgr)
        val txStatus = txMgr.getTransaction(tx)

        val subtitle = subtitleDao.updateVideo(input.pId, input.videoType, this::buildVideoFilename) ?: throw IllegalArgumentException("pId ${input.pId} is invalid.")
        val videoPath = config.videoPath(subtitle, input.videoType) ?: throw RuntimeException("failed to build videoPath.")

        // ファイルコピー
        try {
            BufferedInputStream(video.inputStream).use { inputStream ->
                BufferedOutputStream(FileOutputStream(videoPath)).use { outputStream ->
                    StreamUtil.pumpStream(inputStream, outputStream)
                }
            }
        } catch (e: Exception) {
            txMgr.rollback(txStatus)
            throw e
        }

        return subtitle
    }

    /**
     * 放送の動画を削除します
     */
    fun deleteSubtitleVideo(target: DeleteSubtitleVideoInput, physical: Boolean) {
        val tx = TransactionTemplate(txMgr)
        val txStatus = txMgr.getTransaction(tx)
        val mitaPath = config.recFolderPath.toPath().resolve("mita")
        val moved = mutableListOf<VideoMovementResult>()
        val rollback = {
            moved.forEach { result ->
                try {
                    Files.move(result.dst, result.src)
                    makeDlnaStructure.process(result.src.toFile())
                    logger.info("[deleteSubtitleVideo] Rollback success: pId=${target.pId}, videoType=${result.videoType}, src=${result.dst}, dst=${result.src}")
                } catch (e: IOException) {
                    logger.error("[deleteSubtitleVideo] Rollback failed: pId=${target.pId}, videoType=${result.videoType}, src=${result.dst}, dst=${result.src}", e)
                }
            }
        }

        // DB から動画を削除
        val (oldSubtitle) = subtitleDao.deleteVideo(target.pId, target.videoTypes) ?: return

        // ファイルを mita に移動
        target.videoTypes.forEach { videoType ->
            val file = config.videoPath(oldSubtitle, videoType) ?: return@forEach
            val src = file.toPath()
            val dst = mitaPath.resolve(file.name)
            try {
                Files.move(src, dst, StandardCopyOption.REPLACE_EXISTING)
                makeDlnaStructure.delete(file)
                moved.add(VideoMovementResult(src, dst, videoType))
                if(!physical) {
                    logger.info("[deleteSubtitleVideo] Moved: pId=${target.pId}, videoType=${videoType}, src=${src}, dst=${dst}")
                }
            } catch(e: IOException) {
                logger.error("[deleteSubtitleVideo] Failed to move file: pId=${target.pId}, videoType=${videoType}, src=${src}, dst=${dst}", e)
                txMgr.rollback(txStatus)
                rollback()
                throw DeleteVideoFailedException(target.pId, videoType, file, e)
            }
        }

        // トランザクションをコミット
        txMgr.commit(txStatus)

        // ファイルを物理削除
        if(physical) {
            val itr = moved.iterator()
            while (itr.hasNext()) {
                val result = itr.next()
                try {
                    Files.deleteIfExists(result.dst)
                    itr.remove()
                    logger.info("[deleteSubtitleVideo] Deleted: pId=${target.pId}, videoType=${result.videoType}, path=${result.src}")
                } catch (e: IOException) {
                    logger.error("[deleteSubtitleVideo] Failed to delete file: pId=${target.pId}, videoType=${result.videoType}, path=${result.dst}", e)
                    rollback()
                    throw DeleteVideoFailedException(target.pId, result.videoType, result.src.toFile(), e)
                }
            }
        }
    }

    /**
     * 動画ファイル移動実績
     */
    private data class VideoMovementResult(
        /** 移動元 */
        val src: Path,

        /** 移動先 */
        val dst: Path,

        /** 動画種別 */
        val videoType: VideoType,
    )

    /**
     * dropInfo を取得します
     */
    fun loadDropInfo(subtitle: Subtitle): DropInfoSummary? {
        val dropInfoFile = config.dropInfoPath(subtitle) ?: return null
        if(!dropInfoFile.exists()) {
            return null
        }

        val details = mutableListOf<DropInfoDetail>()
        dropInfoFile.bufferedReader().use { reader ->
            for(line in reader.lineSequence()) {
                val m = regexDropInfo.matchEntire(line)
                if(m != null) {
                    details.add(DropInfoDetail(
                        pid = m.groupValues[1].toInt(16),
                        total = m.groupValues[2].toLong(),
                        drop = m.groupValues[3].toLong(),
                        scrambling = m.groupValues[4].toLong(),
                    ))
                }
            }
        }
        return DropInfoSummary(details)
    }

    /**
     * 指定されたチャンネルのライブを開始します
     *
     * @param stationId チャンネルID
     * @param liveQuality ライブ品質
     */
    fun startLive(stationId: Long, liveQuality: LiveQuality): LiveResult {
        val station = stationDao.get(stationId)
        if(station == null ||
           station.digitalStationBand == Station.DigitalStationBand.RADIO ||
           station.digitalStationBand == Station.DigitalStationBand.UNDEFINED) {
            throw IllegalArgumentException("The specified station does not support live broadcasting. stationId=$stationId")
        }

        // 既に m3u8 ファイルが存在する場合はそちらを優先
        var liveId = "${stationId}_${liveQuality.qualityName}"
        var preferredBufferTime = config.liveBufferTime.toLong()
        Files.list(config.livePath.toPath())
            .asSequence()
            .map { it.fileName.toString() }
            .any { it == "$liveId.m3u8" }
            .also { existsM3u8 ->
                if(existsM3u8) {
                    return LiveResult(
                        liveId,
                        m3u8Uri = config.httpMediaMapPath.resolve("live/$liveId.m3u8"),
                        preferredBufferTime = (preferredBufferTime - getLiveDuration(liveId).toSeconds())
                            .coerceAtLeast(0L)
                            .let { Duration.ofSeconds(it) }
                    )
                }
            }

        // 指定されたチャンネルが録画中の場合は、録画中の TS から再生させる
        val nowRecording = nowRecordingDao.getByStation(stationId)
        val videoSrc = if(nowRecording == null) {
            stationId.toString()
        } else {
            val m2pFilename = m2pToLiveFilename(nowRecording.recFilename)
            liveId = "${m2pFilename.substringBeforeLast(".")}_${liveQuality.qualityName}"
            preferredBufferTime = config.chasingPlaybackBufferTime.toLong()
            m2pFilename
        }

        // ライブプロセスを起動する
        logger.info("[startLive][$liveId] Starting live stream.")
        ProcessBuilder()
            .command(
                config.perlPath("live_process_starter.pl").absolutePath,
                liveQuality.code.toString(),
                "/tv/live",
                videoSrc
            )
            .redirectInput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.PIPE)
            .directory(config.perlToolPath)
            .start()
            .pipeLog("[startLive][$liveId]", logger)
            .waitFor()
            .also {
                if(it != 0) {
                    val message = "live_process_starter.pl terminated with exit code $it."
                    logger.error("[      ][startLive][$liveId] $message")
                    throw RuntimeException(message)
                }
            }
        logger.info("[startLive][$liveId] Started live stream.")

        return LiveResult(
            liveId,
            m3u8Uri = config.httpMediaMapPath.resolve("live/$liveId.m3u8"),
            preferredBufferTime = Duration.ofSeconds(preferredBufferTime)
        )
    }

    /**
     * ライブのバッファされている秒数を取得します
     *
     * @param liveId ライブID
     */
    fun getLiveDuration(liveId: String): Duration {
        val m3u8File = File(config.livePath, "$liveId.m3u8")
        if(!m3u8File.isFile) {
            return Duration.ZERO
        }

        val firstLine = AtomicReference("")
        val exitCode = ProcessBuilder()
            .command(
                "ffprobe",
                "-i",
                m3u8File.absolutePath,
                "-select_streams",
                "v:0",
                "-show_entries",
                "format=start_time",
                "-v",
                "quiet",
                "-of",
                "csv=p=0"
            )
            .redirectInput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.DISCARD)
            .start()
            .readFirstLine(firstLine)
            .waitFor()
        if(exitCode != 0) {
            return Duration.ZERO
        }

        return try {
            Duration.ofSeconds(firstLine.get().toDouble().toLong())
        } catch (_: java.lang.NumberFormatException) {
            Duration.ZERO
        }
    }
//        Files.list(config.livePath.toPath())
//            .asSequence()
//            .map { it.fileName.toString() }
//            .filter { it.startsWith(liveId) && it.endsWith(".ts") }
//            .count()
//            .let { Duration.ofSeconds(it * 10L) }

    /**
     * ライブを停止します
     *
     * @param liveId ライブID
     */
    fun stopLive(liveId: String) {
        val m3u8File = File(config.livePath, "$liveId.m3u8")
        if(!m3u8File.isFile) {
            return
        }

        logger.info("[stopLive][$liveId] Stopping live stream.")
        ProcessBuilder()
            .command(
                config.perlPath("live_stop.pl").absolutePath,
                liveId,
            )
            .redirectInput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.PIPE)
            .directory(config.perlToolPath)
            .start()
            .pipeLog("[stopLive][$liveId]", logger)
            .waitFor()
            .also {
                if(it != 0) {
                    val message = "live_stop.pl terminated with exit code $it."
                    logger.error(message)
                    throw RuntimeException(message)
                }
            }
        logger.info("[stopLive][$liveId] Stopped live stream.")
    }

    /**
     * 全てのライブを停止します
     */
    fun stopLiveAll() =
        liveInfoDao.findLiveIds().also {
            logger.info("[stopAllLive] ${it.size} live stream(s) will be stopped.")
            it.forEach { liveInfo -> stopLive(liveInfo) }
            logger.info("[stopAllLive] ${it.size} live stream(s) has been stopped.")
        }


    companion object {
        private val regexDropInfo = Regex("""^pid=\s*0x([0-9a-f]+),\s*total=\s*(\d+),\s*drop=\s*(\d+),\s*scrambling=\s*(\d+)$""", RegexOption.IGNORE_CASE)

        private val regexM2pToLive = Regex("""^-1-""")

        /**
         * "-1-" で始まるファイル名を "A-" に変換する (実際には "a-" に変換される)
         */
        private fun m2pToLiveFilename(filename: String) =
            regexM2pToLive.replace(filename, "a-")
    }
}
