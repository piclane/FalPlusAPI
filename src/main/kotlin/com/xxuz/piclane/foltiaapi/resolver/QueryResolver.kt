package com.xxuz.piclane.foltiaapi.resolver

import com.xxuz.piclane.foltiaapi.dao.KeywordGroupDao
import com.xxuz.piclane.foltiaapi.dao.ProgramDao
import com.xxuz.piclane.foltiaapi.dao.StationDao
import com.xxuz.piclane.foltiaapi.dao.SubtitleDao
import com.xxuz.piclane.foltiaapi.model.KeywordGroup
import com.xxuz.piclane.foltiaapi.model.vo.*
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

    @Autowired
    private val keywordGroupDao: KeywordGroupDao,
) : GraphQLQueryResolver {
    fun subtitles(query: SubtitleQueryInput?, page: Int, pageRows: Int) =
        subtitleDao.find(query, page, pageRows)

    fun programs(query: ProgramQueryInput?, page: Int, pageRows: Int) =
        programDao.find(query, page, pageRows)

    fun stations(query: StationQueryInput?): StationResult =
        stationDao.find(query)

    fun keywordGroups(query: KeywordGroupQueryInput?): List<KeywordGroup> =
        keywordGroupDao.find(query)
}
