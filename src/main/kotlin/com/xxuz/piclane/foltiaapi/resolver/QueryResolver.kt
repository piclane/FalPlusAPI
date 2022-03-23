package com.xxuz.piclane.foltiaapi.resolver

import com.xxuz.piclane.foltiaapi.dao.ProgramDao
import com.xxuz.piclane.foltiaapi.dao.StationDao
import com.xxuz.piclane.foltiaapi.dao.SubtitleDao
import com.xxuz.piclane.foltiaapi.model.vo.ProgramQueryInput
import com.xxuz.piclane.foltiaapi.model.vo.StationQueryInput
import com.xxuz.piclane.foltiaapi.model.vo.StationResult
import com.xxuz.piclane.foltiaapi.model.vo.SubtitleQueryInput
import graphql.kickstart.tools.GraphQLQueryResolver
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
@Suppress("unused")
class QueryResolver(
    @Autowired
    private val subtitleDao: SubtitleDao,

    @Autowired
    private val programDao: ProgramDao,

    @Autowired
    private val stationDao: StationDao,
) : GraphQLQueryResolver {
    fun subtitles(query: SubtitleQueryInput?, page: Int) =
        subtitleDao.find(query, page)

    fun programs(query: ProgramQueryInput?, page: Int) =
        programDao.find(query, page)

    fun stations(query: StationQueryInput?): StationResult =
        stationDao.find(query)
}
