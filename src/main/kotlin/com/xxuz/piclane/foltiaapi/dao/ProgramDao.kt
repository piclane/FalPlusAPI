package com.xxuz.piclane.foltiaapi.dao

import com.xxuz.piclane.foltiaapi.model.Program
import com.xxuz.piclane.foltiaapi.model.vo.ProgramQueryInput
import com.xxuz.piclane.foltiaapi.model.vo.ProgramResult
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.Cacheable
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

/**
 * 番組Dao
 */
@Repository
class ProgramDao(
        @Autowired
        private val jt: NamedParameterJdbcTemplate
) {
    /**
     * ID から放送を取得します
     */
    @Cacheable("program")
    fun get(tId: Long): Program? =
        try {
            jt.queryForObject(
                """
                SELECT
                    *
                FROM
                    foltia_program
                WHERE
                    tid = :tId
                """,
                mutableMapOf(
                    "tId" to tId
                ),
                RowMapperImpl
            )
        } catch (e: EmptyResultDataAccessException) {
            null
        }

    /**
     * 番組を検索します
     *
     * @param query クエリ
     * @param page ページインデックス
     * @param pageRows ページあたりの行数
     */
    fun find(query: ProgramQueryInput?, page: Int, pageRows: Int): ProgramResult {
        val conditions = mutableListOf<String>(
            "tid NOT IN (:tIdKeyword, :tIdEpg)"
        )
        val params = mutableMapOf<String, Any>(
            "tIdKeyword" to Program.TID_KEYWORD,
            "tIdEpg" to Program.TID_EPG,
            "limit" to pageRows,
            "offset" to pageRows * page
        )

        if(query?.firstLightAfter != null) {
            conditions.add("P.firstlight >= :firstLightAfter")
            params["firstLightAfter"] = query.firstLightAfter.year * 100 + query.firstLightAfter.monthValue
        }
        if(query?.firstLightBefore != null) {
            conditions.add("P.firstlight <= :firstLightBefore")
            params["firstLightBefore"] = query.firstLightBefore.year * 100 + query.firstLightBefore.monthValue
        }
        if(query?.titleContains != null) {
            conditions.add("""(
                P.title LIKE :titleContains OR
                P.shorttitle LIKE :titleContains OR
                P.titleyomi LIKE :titleContains OR
                P.titleen LIKE :titleContains
            )""".trimIndent())
            params["titleContains"] = "%${query.titleContains}%"
        }
        if(query?.hasRecording == true) {
            conditions.add("""EXISTS(
                SELECT 1
                FROM foltia_subtitle AS S 
                WHERE S.tid = P.tid AND (
                    (S.m2pfilename IS NOT NULL AND EXISTS(SELECT 1 FROM foltia_m2pfiles AS TS WHERE TS.m2pfilename = S.m2pfilename)) OR 
                    (S.pspfilename IS NOT NULL AND EXISTS(SELECT 1 FROM foltia_mp4files AS SD WHERE SD.mp4filename = S.pspfilename)) OR 
                    (S.mp4hd IS NOT NULL AND EXISTS(SELECT 1 FROM foltia_hdmp4files AS HD WHERE HD.hdmp4filename = S.mp4hd))
                )
            )""".trimIndent())
        } else if(query?.hasRecording == false) {
            conditions.add("""EXISTS(
                SELECT 1
                FROM foltia_subtitle AS S 
                WHERE S.tid = P.tid AND (
                    S.m2pfilename IS NULL AND
                    S.pspfilename IS NULL AND
                    S.mp4hd IS NULL
                )
            )""".trimIndent())
        }

        val where = if(conditions.isEmpty()) "" else "WHERE ${conditions.joinToString(" AND ")}"
        val data =  jt.query(
            """
            SELECT
                *
            FROM
                foltia_program AS P
            $where
            ORDER BY firstlight DESC, tid DESC 
            LIMIT :limit
            OFFSET :offset
            """,
            params,
            RowMapperImpl
        )
        val total = jt.queryForObject(
            """
            SELECT 
                COUNT(*)
            FROM
                foltia_program AS P
            $where
            """,
            params,
            Int::class.java
        )

        return ProgramResult(page, total ?: 0, data)
    }

    /**
     * ResultSet から Program にマッピングする RowMapper
     */
    private object RowMapperImpl : RowMapper<Program> {
        override fun mapRow(rs: ResultSet, rowNum: Int): Program =
            Program(
                tId = rs.getLong("tid"),
                title = rs.getString("title"),
                firstLight = rs.getInt("firstlight").let {
                    if (rs.wasNull())
                        null
                    else {
                        val year = floor(it / 100.0).toInt()
                        val month = min(12, max(1, it % 100))
                        LocalDate.of(year, month, 1)
                    }
                },
                aspect = rs.getLong("aspect"),
                shortTitle = rs.getString("shorttitle") ?: "",
                titleYomi = rs.getString("titleyomi") ?: "",
                titleEn = rs.getString("titleen") ?: "",
            )
    }
}


