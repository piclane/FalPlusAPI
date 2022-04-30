package com.xxuz.piclane.foltiaapi.resolver

import com.xxuz.piclane.foltiaapi.dao.EpgDao
import com.xxuz.piclane.foltiaapi.model.Epg
import com.xxuz.piclane.foltiaapi.model.Station
import com.xxuz.piclane.foltiaapi.model.vo.EpgQueryInput
import graphql.kickstart.tools.GraphQLResolver
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
@Suppress("unused")
class StationResolver(
    @Autowired
    private val epgDao: EpgDao,
) : GraphQLResolver<Station> {
    fun epg(station: Station, query: EpgQueryInput?): List<Epg> =
        epgDao.find(query, station.stationId)

    fun epgNow(station: Station): Epg? =
        try {
            epgDao.find(EpgQueryInput.now(), station.stationId).first()
        } catch (_: java.util.NoSuchElementException) {
            null
        }
}
