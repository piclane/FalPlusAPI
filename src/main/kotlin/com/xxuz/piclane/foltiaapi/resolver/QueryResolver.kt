package com.xxuz.piclane.foltiaapi.resolver

import com.xxuz.piclane.foltiaapi.dao.KeywordGroupDao
import com.xxuz.piclane.foltiaapi.dao.ProgramDao
import com.xxuz.piclane.foltiaapi.dao.StationDao
import com.xxuz.piclane.foltiaapi.dao.SubtitleDao
import com.xxuz.piclane.foltiaapi.foltia.FoltiaConfig
import com.xxuz.piclane.foltiaapi.model.DiskInfo
import com.xxuz.piclane.foltiaapi.model.KeywordGroup
import com.xxuz.piclane.foltiaapi.model.Subtitle
import com.xxuz.piclane.foltiaapi.model.vo.*
import graphql.kickstart.tools.GraphQLQueryResolver
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.nio.file.Files

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

    @Autowired
    private val foltiaConfig: FoltiaConfig,
) : GraphQLQueryResolver {
    fun subtitle(pId: Long): Subtitle =
        subtitleDao.get(pId) ?: throw IllegalArgumentException("pId $pId is not exists.")

    fun subtitles(query: SubtitleQueryInput?, offset: Int, limit: Int) =
        subtitleDao.find(query, offset, limit)

    fun programs(query: ProgramQueryInput?, offset: Int, limit: Int) =
        programDao.find(query, offset, limit)

    fun stations(query: StationQueryInput?): StationResult =
        stationDao.find(query)

    fun keywordGroups(query: KeywordGroupQueryInput?): List<KeywordGroup> =
        keywordGroupDao.find(query)

    fun diskInfo() =
        Files.getFileStore(foltiaConfig.recFolderPath.toPath()).let {
            DiskInfo(
                totalBytes = it.totalSpace,
                usableBytes= it.usableSpace
            )
        }
}
