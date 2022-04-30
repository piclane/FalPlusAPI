package com.xxuz.piclane.foltiaapi.resolver

import com.xxuz.piclane.foltiaapi.dao.StationDao
import com.xxuz.piclane.foltiaapi.dao.SubtitleDao
import com.xxuz.piclane.foltiaapi.foltia.FoltiaManipulation
import com.xxuz.piclane.foltiaapi.job.JobManager
import com.xxuz.piclane.foltiaapi.model.LiveQuality
import com.xxuz.piclane.foltiaapi.model.vo.LiveResult
import com.xxuz.piclane.foltiaapi.model.Station
import com.xxuz.piclane.foltiaapi.model.Subtitle
import com.xxuz.piclane.foltiaapi.model.vo.*
import graphql.kickstart.tools.GraphQLMutationResolver
import kotlinx.coroutines.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import javax.annotation.PreDestroy
import javax.servlet.http.Part

@Component
@Suppress("unused")
@ObsoleteCoroutinesApi
class MutationResolver(
    @Autowired
    private val subtitleDao: SubtitleDao,

    @Autowired
    private val stationDao: StationDao,

    @Autowired
    private val foltiaManipulation: FoltiaManipulation,

    @Autowired
    private val jobManager: JobManager,
) : GraphQLMutationResolver {
    /** 動画削除用スレッドプール */
    private val deleteVideoThreadPoolContext = newFixedThreadPoolContext(4, "DeleteVideoThreadPool")

    @PreDestroy
    fun shutdown() {
        deleteVideoThreadPoolContext.close()
    }

    fun updateSubtitle(input: SubtitleUpdateInput): Subtitle {
        subtitleDao.update(input)
        return subtitleDao.get(input.pId) ?: throw IllegalArgumentException("pId ${input.pId} does not exist.")
    }

    fun uploadSubtitleVideo(input: UploadSubtitleVideoInput, video: Part): Subtitle =
        foltiaManipulation.uploadSubtitleVideo(input, video)

    fun deleteSubtitleVideo(input: List<DeleteSubtitleVideoInput>, physical: Boolean): String {
        val jobs = jobManager.issue(deleteVideoThreadPoolContext)
        input.forEach {
            jobs.schedule {
                foltiaManipulation.deleteSubtitleVideo(it, physical)
            }
        }
        jobs.start()
        return jobs.jobId
    }

    fun deleteSubtitleVideoByQuery(input: SubtitleQueryInput, physical: Boolean): String {
        val jobs = jobManager.issue(deleteVideoThreadPoolContext)
        if(input.videoTypes?.isEmpty() == true) {
            return jobs.jobId
        }
        val videoTypes = input.videoTypes!!
        val limit = 100
        var offset = 0
        while(true) {
            val result = subtitleDao.find(input, offset, limit)
            if(result.data.isEmpty()) {
                break
            }
            result.data
                .asSequence()
                .flatMap { subtitle ->
                    videoTypes.map { videoType ->
                        DeleteSubtitleVideoInput(
                            pId = subtitle.pId,
                            videoTypes = setOf(videoType),
                        )
                    }
                }
                .forEach {
                    jobs.schedule {
                        foltiaManipulation.deleteSubtitleVideo(it, physical)
                    }
                }
            offset += limit
        }
        jobs.start()
        return jobs.jobId
    }

    fun updateStation(input: StationUpdateInput): Station {
        stationDao.update(input)
        return stationDao.get(input.stationId) ?: throw IllegalArgumentException("stationId ${input.stationId} does not exist.")
    }

    fun startLive(stationId: Long, liveQuality: LiveQuality): LiveResult =
        foltiaManipulation.startLive(stationId, liveQuality)

    fun stopLive(liveId: String) =
        foltiaManipulation.stopLive(liveId)

    fun stopLiveAll() =
        foltiaManipulation.stopLiveAll()

    fun startTranscode() {
        foltiaManipulation.startTranscode()
    }
}
