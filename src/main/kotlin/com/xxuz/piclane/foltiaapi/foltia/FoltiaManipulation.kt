package com.xxuz.piclane.foltiaapi.foltia

import com.xxuz.piclane.foltiaapi.dao.StationDao
import com.xxuz.piclane.foltiaapi.dao.SubtitleDao
import com.xxuz.piclane.foltiaapi.model.DropInfoDetail
import com.xxuz.piclane.foltiaapi.model.DropInfoSummary
import com.xxuz.piclane.foltiaapi.model.Subtitle
import com.xxuz.piclane.foltiaapi.model.VideoType
import com.xxuz.piclane.foltiaapi.model.vo.DeleteSubtitleVideoInput
import com.xxuz.piclane.foltiaapi.model.vo.UploadSubtitleVideoInput
import com.xxuz.piclane.foltiaapi.util.StreamUtil
import com.xxuz.piclane.foltiaapi.util.pipeLog
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.FileOutputStream
import java.time.format.DateTimeFormatter
import java.util.concurrent.Executors
import javax.servlet.http.Part

@Repository
class FoltiaManipulation(
    @Autowired
    private val config: FoltiaConfig,

    @Autowired
    private val subtitleDao: SubtitleDao,

    @Autowired
    private val stationDao: StationDao,

    @Autowired
    private val txMgr: PlatformTransactionManager,
) {
    private val logger = LoggerFactory.getLogger(FoltiaManipulation::class.java)

    private var esNextId = 1
    private val es = Executors.newFixedThreadPool(5) { r ->
        Thread(r).also {
            it.name = "Foltia Manipulation Thread - ${esNextId++}"
            it.isDaemon = true
            it.priority = Thread.MIN_PRIORITY
        }
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
            VideoType.SD -> "MAQ-$base.mp4"
            VideoType.HD -> "MAQ-$base.mp4"
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
    fun deleteSubtitleVideo(target: DeleteSubtitleVideoInput) {
        es.submit {
            if(target.physical)
                deleteSubtitleVideoPhysically(target)
            else
                deleteSubtitleVideoLogically(target)
        }
    }

    /**
     * 放送の動画を論理削除します
     */
    private fun deleteSubtitleVideoLogically(target: DeleteSubtitleVideoInput) {
        val subtitle = subtitleDao.get(target.pId) ?: return
        val deleteMoviePl = config.perlPath("deletemovie.pl")
        target.videoTypes.forEach { videoType ->
            val filename = subtitle.videoFilename(videoType)
            ProcessBuilder()
                .directory(config.perlToolPath)
                .command(deleteMoviePl.absolutePath, filename)
                .redirectError(ProcessBuilder.Redirect.PIPE)
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .start()
                .pipeLog("[deleteSubtitleVideoLogically] ", logger)
                .waitFor()
        }
    }

    /**
     * 放送の動画を物理削除します
     */
    private fun deleteSubtitleVideoPhysically(target: DeleteSubtitleVideoInput) {
        val tx = TransactionTemplate(txMgr)
        val txStatus = txMgr.getTransaction(tx)

        // DB から動画を削除
        val (oldSubtitle) = subtitleDao.deleteVideo(target.pId, target.videoTypes) ?: return

        // 動画ファイルを削除
        if(config.tsVideoPath(oldSubtitle)?.delete() != true) {
            txMgr.rollback(txStatus)
        }
    }

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

    companion object {
        private val regexDropInfo = Regex("""^pid=\s*0x([0-9a-f]+),\s*total=\s*(\d+),\s*drop=\s*(\d+),\s*scrambling=\s*(\d+)$""", RegexOption.IGNORE_CASE)
    }
}
