package com.xxuz.piclane.foltiaapi.resolver

import com.xxuz.piclane.foltiaapi.dao.KeywordGroupDao
import com.xxuz.piclane.foltiaapi.dao.ProgramDao
import com.xxuz.piclane.foltiaapi.dao.StationDao
import com.xxuz.piclane.foltiaapi.foltia.FoltiaConfig
import com.xxuz.piclane.foltiaapi.foltia.FoltiaManipulation
import com.xxuz.piclane.foltiaapi.model.*
import graphql.kickstart.tools.GraphQLResolver
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.net.URI

@Component
@Suppress("unused")
class SubtitleResolver(
    @Autowired
    private val stationDao: StationDao,

    @Autowired
    private val programDao: ProgramDao,

    @Autowired
    private val keywordGroupDao: KeywordGroupDao,

    @Autowired
    private val foltiaConfig: FoltiaConfig,

    @Autowired
    private val foltiaManipulation: FoltiaManipulation
) : GraphQLResolver<Subtitle> {
    fun station(subtitle: Subtitle): Station =
        stationDao.get(subtitle.stationId) ?: throw IllegalArgumentException("stationId ${subtitle.stationId} is invalid.")

    fun program(subtitle: Subtitle): Program =
        programDao.get(subtitle.tId) ?: throw IllegalArgumentException("tId ${subtitle.tId} is invalid.")

    fun keywordGroups(subtitle: Subtitle): List<KeywordGroup>? =
        if(subtitle.tId == Program.TID_KEYWORD)
            keywordGroupDao.find(subtitle.countNo ?: throw IllegalArgumentException("subtitle.countNo is null"))
        else
            null

    fun tsVideoUri(subtitle: Subtitle): URI? =
        foltiaConfig.tsVideoUri(subtitle)

    fun sdVideoUri(subtitle: Subtitle): URI? =
        foltiaConfig.sdVideoUri(subtitle)

    fun hdVideoUri(subtitle: Subtitle): URI? =
        foltiaConfig.hdVideoUri(subtitle)

    fun dropInfoSummary(subtitle: Subtitle): DropInfoSummary? =
        foltiaManipulation.loadDropInfo(subtitle)

    fun dropInfoDetail(subtitle: Subtitle): List<DropInfoDetail>? =
        foltiaManipulation.loadDropInfo(subtitle)?.details

    fun thumbnailUri(subtitle: Subtitle): URI? =
        foltiaConfig.thumbnailUri(subtitle)

    fun thumbnailUris(subtitle: Subtitle): List<URI>? =
        foltiaConfig.thumbnailUris(subtitle)
}
