package com.xxuz.piclane.foltiaapi.dao

import com.xxuz.piclane.foltiaapi.model.Epg
import com.xxuz.piclane.foltiaapi.model.NowRecording
import com.xxuz.piclane.foltiaapi.model.Station
import com.xxuz.piclane.foltiaapi.model.vo.EpgQueryInput
import com.xxuz.piclane.foltiaapi.model.vo.StationResult
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.sql.ResultSet

/**
 * Epg
 */
@Repository
class EpgDao(
    @Autowired
    private val jt: NamedParameterJdbcTemplate
)  {
    /**
     * ID から EPG を取得します
     *
     * @param epgId EPG ID
     */
    fun get(epgId: Long): Epg? =
        try {
            jt.queryForObject(
                """
                SELECT
                    
                FROM
                    foltia_epg AS E
                WHERE
                    E.epgid = :epgId
                """,
                mutableMapOf(
                    "epgId" to epgId
                ),
                RowMapperImpl
            )
        } catch (e: EmptyResultDataAccessException) {
            null
        }

    /**
     * 指定したチャンネルの EPG を取得します。
     *
     * @param query EPG クエリ入力
     * @param stationId チャンネルID
     */
    fun find(query: EpgQueryInput?, stationId: Long): List<Epg> {
        val conditions = mutableListOf(
            "S.stationid = :stationId"
        )
        val params = mutableMapOf<String, Any>(
            "stationId" to stationId
        )

        if(query?.startBefore != null) {
            conditions.add("E.startdatetime <= :startBefore")
            params["startBefore"] = query.startBefore.toLong()
        }
        if(query?.startAfter != null) {
            conditions.add("E.startdatetime >= :startAfter")
            params["startAfter"] = query.startAfter.toLong()
        }
        if(query?.endBefore != null) {
            conditions.add("E.enddatetime < :endBefore")
            params["endBefore"] = query.endBefore.toLong()
        }
        if(query?.endAfter != null) {
            conditions.add("E.enddatetime > :endAfter")
            params["endAfter"] = query.endAfter.toLong()
        }

        return jt.query(
            """
                SELECT
                    E.*
                FROM
                    foltia_epg AS E
                INNER JOIN 
                    foltia_station AS S ON S.ontvcode = E.ontvchannel
                WHERE
                    ${conditions.joinToString(" AND ")}
                ORDER BY E.startdatetime
            """,
            params,
            RowMapperImpl
        )
    }

    /**
     * ResultSet から Epg にマッピングする RowMapper
     */
    private object RowMapperImpl : RowMapper<Epg> {
        override fun mapRow(rs: ResultSet, rowNum: Int): Epg =
            Epg(
                epgId = rs.getLong("epgid"),
                startDateTime = rs.getLong("startdatetime").toLocalDateTime(),
                endDateTime = rs.getLong("enddatetime").toLocalDateTime(),
                onTvChannel = rs.getString("ontvchannel"),
                title = rs.getString("epgtitle") ?: "未定",
                description = rs.getString("epgdesc") ?: "",
                category = rs.getString("epgcategory").let { if(it == null) Epg.Category.ETC else Epg.Category.codeOf(it)}
            )
    }
}
