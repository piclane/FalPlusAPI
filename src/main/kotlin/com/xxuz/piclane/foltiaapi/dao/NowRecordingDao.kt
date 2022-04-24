package com.xxuz.piclane.foltiaapi.dao

import com.xxuz.piclane.foltiaapi.model.NowRecording
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.sql.ResultSet

/**
 * NowRecording
 */
@Repository
class NowRecordingDao(
    @Autowired
    private val jt: NamedParameterJdbcTemplate
)  {
    /**
     * 指定したチャンネルの録画中情報を取得します。
     *
     * @param stationId チャンネルID
     */
    fun getByStation(stationId: Long): NowRecording? =
        try {
            jt.queryForObject(
                """
                SELECT
                    N.*
                FROM
                    foltia_nowrecording AS N
                INNER JOIN 
                    foltia_subtitle S on N.pid = S.pid
                WHERE
                    S.stationid = :stationId
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
     * ResultSet から Station にマッピングする RowMapper
     */
    private object RowMapperImpl : RowMapper<NowRecording> {
        override fun mapRow(rs: ResultSet, rowNum: Int): NowRecording =
            NowRecording(
                pId = rs.getLong("pid"),
                recFilename = rs.getString("recfilename"),
                device = rs.getString("device"),
                lastUpdate =  rs.getTimestamp("lastupdate").toOffsetDateTime().orElse(null)
            )
    }
}
