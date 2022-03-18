package com.xxuz.piclane.foltiaapi.resolver

import com.xxuz.piclane.foltiaapi.dao.ProgramDao
import com.xxuz.piclane.foltiaapi.dao.SubtitleDao
import com.xxuz.piclane.foltiaapi.model.vo.ProgramQueryInput
import com.xxuz.piclane.foltiaapi.model.vo.SubtitleQueryInput
import graphql.kickstart.tools.GraphQLQueryResolver
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
@Suppress("unused")
class QueryResolver(
    @Autowired
    val subtitleDao: SubtitleDao,

    @Autowired
    val programDao: ProgramDao
) : GraphQLQueryResolver {
    fun subtitles(query: SubtitleQueryInput?, page: Int) =
        subtitleDao.find(query, page)

    fun programs(query: ProgramQueryInput?, page: Int) =
        programDao.find(query, page)
}
