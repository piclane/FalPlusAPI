package com.xxuz.piclane.foltiaapi.resolver

import com.xxuz.piclane.foltiaapi.dao.ProgramDao
import com.xxuz.piclane.foltiaapi.dao.StationDao
import com.xxuz.piclane.foltiaapi.foltia.FoltiaManipulation
import com.xxuz.piclane.foltiaapi.model.*
import graphql.kickstart.tools.GraphQLResolver
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
@Suppress("unused")
class SubtitleResolver(
    @Autowired
    private val stationDao: StationDao,

    @Autowired
    private val programDao: ProgramDao,

    @Autowired
    private val foltiaManipulation: FoltiaManipulation
) : GraphQLResolver<Subtitle> {
    fun station(subtitle: Subtitle): Station =
        stationDao.get(subtitle.stationId) ?: throw IllegalArgumentException("stationId ${subtitle.stationId} is invalid.")

    fun program(subtitle: Subtitle): Program =
        programDao.get(subtitle.tId) ?: throw IllegalArgumentException("tId ${subtitle.tId} is invalid.")

    fun dropInfoSummary(subtitle: Subtitle): DropInfoSummary? =
        foltiaManipulation.loadDropInfo(subtitle)

    fun dropInfoDetail(subtitle: Subtitle): List<DropInfoDetail>? =
        foltiaManipulation.loadDropInfo(subtitle)?.details
}
