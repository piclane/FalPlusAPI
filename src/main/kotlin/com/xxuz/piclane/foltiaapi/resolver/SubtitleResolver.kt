package com.xxuz.piclane.foltiaapi.resolver

import com.xxuz.piclane.foltiaapi.dao.ProgramDao
import com.xxuz.piclane.foltiaapi.dao.StationDao
import com.xxuz.piclane.foltiaapi.model.Program
import com.xxuz.piclane.foltiaapi.model.Station
import com.xxuz.piclane.foltiaapi.model.Subtitle
import graphql.kickstart.tools.GraphQLResolver
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class SubtitleResolver(
    @Autowired
    private val stationDao: StationDao,

    @Autowired
    private val programDao: ProgramDao,
) : GraphQLResolver<Subtitle> {
    fun station(subtitle: Subtitle): Station =
        stationDao.get(subtitle.stationId) ?: throw IllegalArgumentException("stationId ${subtitle.stationId} is invalid.")

    fun program(subtitle: Subtitle): Program =
        programDao.get(subtitle.tId) ?: throw IllegalArgumentException("tId ${subtitle.tId} is invalid.")
}
