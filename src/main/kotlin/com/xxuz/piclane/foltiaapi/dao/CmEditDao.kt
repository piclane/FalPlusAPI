package com.xxuz.piclane.foltiaapi.dao

import com.xxuz.piclane.foltiaapi.model.CmEdit
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.ResultSetExtractor
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository

/**
 * CM カット Dao
 */
@Repository
class CmEditDao(
    @Autowired
    private val jt: NamedParameterJdbcTemplate,
) {
    /**
     * 指定された放送 ID の CM カット情報を取得します
     */
    fun get(pId: Long): CmEdit =
        try {
            jt.queryForObject(
                "SELECT cmeditdetectthreshold, cmeditmode FROM foltia_subtitlecmeditrule WHERE pid = :pId",
                mapOf(
                    "pId" to pId
                )
            ) { rs, _ ->
                CmEdit.of(
                    CmEdit.DetectThreshold.codeOf(rs.getInt("cmeditdetectthreshold")),
                    CmEdit.RuleSet.codeOf(rs.getInt("cmeditmode")),
                )
            }
        } catch (e: EmptyResultDataAccessException) {
            null
        }.let {
            it ?: CmEdit.of(
                getThreshold(pId),
                getRuleSet(pId)
            )
        }

    private fun getThreshold(pId: Long): CmEdit.DetectThreshold =
        try {
            jt.queryForObject(
                """
                    SELECT R.cmeditdetectthreshold 
                    FROM foltia_station AS R 
                    INNER JOIN foltia_subtitle AS S ON S.stationid = R.stationId
                    WHERE S.pid = :pId
                """.trimIndent(),
                mapOf(
                    "pId" to pId
                )
            ) { rs, _ ->
                CmEdit.DetectThreshold.codeOf(rs.getInt("cmeditdetectthreshold"))
            }
        } catch (e: EmptyResultDataAccessException) {
            null
        }.let {
            it ?: CmEdit.DetectThreshold.OFF
        }

    private fun getRuleSet(pId: Long): CmEdit.RuleSet =
        try {
            if(pId > 0)
                jt.queryForObject(
                    """
                        SELECT R.cmeditmode 
                        FROM foltia_tvrecord AS R
                        INNER JOIN foltia_subtitle AS S ON S.tid = R.tid
                        WHERE S.pid = :pId
                    """.trimIndent(),
                    mapOf(
                        "pId" to pId
                    )
                ) { rs, _ ->
                    CmEdit.RuleSet.codeOf(rs.getInt("cmeditmode"))
                }
            else
                jt.queryForObject(
                    """
                        SELECT KR.cmeditmode 
                        FROM foltia_keywordrec AS KR
                        INNER JOIN foltia_listofkeywordrec AS LOKR ON KR.keywordid = LOKR.keywordid
                        INNER JOIN foltia_subtitle AS S ON S.countno = LOKR.countno
                        WHERE S.pid = :pId
                    """.trimIndent(),
                    mapOf(
                        "pId" to pId
                    )
                ) { rs, _ ->
                    CmEdit.RuleSet.codeOf(rs.getInt("cmeditmode"))
                }
        } catch (e: EmptyResultDataAccessException) {
            null
        }.let {
            it ?: CmEdit.RuleSet.DO_NOTHING
        }
}
