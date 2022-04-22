package com.xxuz.piclane.foltiaapi.resolver

import com.xxuz.piclane.foltiaapi.dao.StationDao
import com.xxuz.piclane.foltiaapi.dao.SubtitleDao
import com.xxuz.piclane.foltiaapi.foltia.FoltiaManipulation
import com.xxuz.piclane.foltiaapi.model.Station
import com.xxuz.piclane.foltiaapi.model.Subtitle
import com.xxuz.piclane.foltiaapi.model.vo.*
import graphql.kickstart.tools.GraphQLMutationResolver
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import javax.servlet.http.Part

@Component
@Suppress("unused")
class MutationResolver(
    @Autowired
    private val subtitleDao: SubtitleDao,

    @Autowired
    private val stationDao: StationDao,

    @Autowired
    private val foltiaManipulation: FoltiaManipulation,
) : GraphQLMutationResolver {
    fun updateSubtitle(input: SubtitleUpdateInput): Subtitle {
        subtitleDao.update(input)
        return subtitleDao.get(input.pId) ?: throw IllegalArgumentException("pId ${input.pId} does not exist.")
    }

    fun uploadSubtitleVideo(input: UploadSubtitleVideoInput, video: Part): Subtitle =
        foltiaManipulation.uploadSubtitleVideo(input, video)

    fun deleteSubtitleVideo(input: List<DeleteSubtitleVideoInput>) {
        input.forEach {
            foltiaManipulation.deleteSubtitleVideo(it)
        }
    }

    fun deleteSubtitleVideoByQuery(input: SubtitleQueryInput, physical: Boolean): Int {
        if(input.videoTypes?.isEmpty() == true) {
            return 0
        }
        val videoTypes = input.videoTypes!!
        val limit = 100
        var offset = 0
        var count = 0
        while(true) {
            val result = subtitleDao.find(input, offset, limit)
            if(result.data.isEmpty()) {
                break
            }
            result.data
                .asSequence()
                .map { subtitle ->
                    DeleteSubtitleVideoInput(
                        pId = subtitle.pId,
                        videoTypes = videoTypes,
                        physical = physical
                    )
                }
                .also {
                    count += it.count() * videoTypes.size
                }
                .forEach {
                    foltiaManipulation.deleteSubtitleVideo(it)
                }
            offset += limit
        }
        return count
    }

    fun updateStation(input: StationUpdateInput): Station {
        stationDao.update(input)
        return stationDao.get(input.stationId) ?: throw IllegalArgumentException("stationId ${input.stationId} does not exist.")
    }

    fun startTranscode() {
        foltiaManipulation.startTranscode()
    }
}
