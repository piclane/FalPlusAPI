package com.xxuz.piclane.foltiaapi.dao

import com.xxuz.piclane.foltiaapi.model.CmEdit
import com.xxuz.piclane.foltiaapi.model.Station
import com.xxuz.piclane.foltiaapi.model.vo.StationQueryInput
import com.xxuz.piclane.foltiaapi.model.vo.StationResult
import com.xxuz.piclane.foltiaapi.model.vo.StationUpdateInput
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
    @Cacheable(cacheNames = ["foltia"], key = "'station:stationId=' + #stationId")
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
        if(query?.digitalStationBands != null && query.digitalStationBands.isNotEmpty()) {
            val options =  query.digitalStationBands.map { it.code }.joinToString(", ")
            conditions.add("digitalstationband IN (${options})")
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
     * チャンネルを更新します
     *
     * @param input チャンネル更新入力
     */
    fun update(input: StationUpdateInput) {
        val sets = mutableListOf<String>()
        val params = mutableMapOf<String, Any>(
            "stationId" to input.stationId
        )

        if(input.stationNameDefined) {
            if(input.stationName.isNullOrBlank()) {
                throw IllegalArgumentException("stationName must not be blank.")
            }
            sets.add("stationname = :stationName")
            params["stationName"] = input.stationName
        }
        if(input.ontvcodeDefined) {
            if(input.ontvcode.isNullOrBlank()) {
                throw IllegalArgumentException("ontvcode must not be blank.")
            }
            sets.add("ontvcode = :ontvcode")
            params["ontvcode"] = input.ontvcode
        }
        if(input.digitalChDefined) {
            if(input.digitalCh == null) {
                throw IllegalArgumentException("digitalCh must not be null.")
            }
            sets.add("digitalch = :digitalCh")
            params["digitalCh"] = input.digitalCh
        }
        if(input.receivingDefined) {
            if(input.receiving == null) {
                throw IllegalArgumentException("receiving must not be null.")
            }
            sets.add("receiving = :receiving")
            params["receiving"] = if(input.receiving) 1 else 0
        }
        if(input.cmEditDetectThresholdDefined) {
            if(input.cmEditDetectThreshold == null) {
                throw IllegalArgumentException("cmEditDetectThreshold must not be null.")
            }
            sets.add("receiving = :cmEditDetectThreshold")
            params["cmeditdetectthreshold"] = input.cmEditDetectThreshold.code
        }

        jt.update(
            """
            UPDATE
                foltia_station
            SET
                ${sets.joinToString(", ")}
            WHERE
                stationid = :stationId
            """,
            params
        )
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
                        CmEdit.DetectThreshold.OFF
                    else
                        CmEdit.DetectThreshold.codeOf(it)
                },
            )
    }
}
