package com.xxuz.piclane.foltiaapi.resolver

import com.xxuz.piclane.foltiaapi.dao.SubtitleDao
import com.xxuz.piclane.foltiaapi.foltia.FoltiaManipulation
import com.xxuz.piclane.foltiaapi.model.Subtitle
import com.xxuz.piclane.foltiaapi.model.vo.DeleteSubtitleVideoInput
import com.xxuz.piclane.foltiaapi.model.vo.SubtitleUpdateInput
import com.xxuz.piclane.foltiaapi.model.vo.UploadSubtitleVideoInput
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

    fun startTranscode() {
        foltiaManipulation.startTranscode()
    }
}
