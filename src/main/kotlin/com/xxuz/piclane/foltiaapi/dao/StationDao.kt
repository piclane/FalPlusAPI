package com.xxuz.piclane.foltiaapi.dao

import com.xxuz.piclane.foltiaapi.model.Station
import com.xxuz.piclane.foltiaapi.model.vo.StationQueryInput
import com.xxuz.piclane.foltiaapi.model.vo.StationResult
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.Cacheable
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.sql.ResultSet

/**
 * チャンネル
 */
@Repository
class StationDao(
        @Autowired
        private val jt: NamedParameterJdbcTemplate
) {
    /**
     * ID からチャンネルを取得します
     */
    @Cacheable("station")
    fun get(stationId: Long): Station? =
        try {
            jt.queryForObject(
                """
                SELECT
                    *
                FROM
                    foltia_station
                WHERE
                    stationId = :stationId
                """,
                mutableMapOf(
                    "stationId" to stationId
                ),
                RowMapperImpl
            )
        } catch (e: EmptyResultDataAccessException) {
            null
        }

    /**
     * チャンネルを検索します
     *
     * @param query クエリ
     */
    fun find(query: StationQueryInput?): StationResult {
        val conditions = mutableListOf(
            "stationid != 0"
        )
        val params = mutableMapOf<String, Any>()

        if(query?.receivableStation != null) {
            conditions.add("receiving = :receiving")
            params["receiving"] = if(query.receivableStation) 1 else 0
        }

        val data = jt.query(
            """
                SELECT
                    *
                FROM
                    foltia_station
                WHERE
                    ${conditions.joinToString(" AND ")}
                ORDER BY stationid
            """,
            params,
            RowMapperImpl
        )

        return StationResult(data.size, data)
    }

    /**
     * ResultSet から Station にマッピングする RowMapper
     */
    private object RowMapperImpl : RowMapper<Station> {
        override fun mapRow(rs: ResultSet, rowNum: Int): Station =
                Station(
                        stationId = rs.getLong("stationid"),
                        stationName = rs.getString("stationname"),
//                        stationRecCh = rs.getLong("stationrecch"),
                        stationCallSign = rs.getString("stationcallsign"),
                        stationUri = rs.getString("stationuri"),
//                        tunerType = rs.getString("tunertype"),
//                        tunerCh = rs.getString("tunerch"),
//                        device = rs.getString("device"),
                        ontvcode = rs.getString("ontvcode"),
                        digitalCh = rs.getLong("digitalch").let { if (rs.wasNull()) null else it },
                        digitalStationBand = rs.getInt("digitalstationband").let {
                            if (rs.wasNull())
                                null
                            else
                                Station.DigitalStationBand.codeOf(it).orElseThrow()
                        },
                        epgName = rs.getString("epgname")?.trim(),
                        receiving = rs.getLong("receiving") == 1L,
                        cmEditDetectThreshold = rs.getInt("cmeditdetectthreshold").let {
                            if (rs.wasNull())
                                null
                            else
                                Station.CmEditDetectThreshold.codeOf(it).orElseThrow()
                        },
                )
    }
}
