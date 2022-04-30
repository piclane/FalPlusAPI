package com.xxuz.piclane.foltiaapi.dao

import com.xxuz.piclane.foltiaapi.model.KeywordGroup
import com.xxuz.piclane.foltiaapi.model.Program
import com.xxuz.piclane.foltiaapi.model.VideoType
import com.xxuz.piclane.foltiaapi.model.vo.KeywordGroupQueryInput
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.sql.ResultSet

/**
 * キーワードグループ Dao
 */
@Repository
class KeywordGroupDao(
    @Autowired
    private val jt: NamedParameterJdbcTemplate
) {
    /**
     * キーワードグループを取得します
     */
    fun get(keywordGroupId: Long): KeywordGroup? =
        try {
            jt.queryForObject(
                """
                SELECT
                    W.*
                FROM
                    foltia_keywordlibword AS W
                WHERE
                    W.keywordgroupid = :keywordGroupId
                """,
                mutableMapOf(
                    "keywordGroupId" to keywordGroupId
                ),
                RowMapperImpl
            )
        } catch (e: EmptyResultDataAccessException) {
            null
        }

    /**
     * キーワード録画の話数からすべてのキーワードグループを取得します
     */
    fun find(countNo: Long): List<KeywordGroup> =
        jt.query(
            """
            SELECT
                W.*
            FROM
                foltia_keywordlibword AS W
            INNER JOIN
                foltia_keywordlibfiles AS F ON F.keywordgroupid = W.keywordgroupid 
            WHERE
                F.countno = :countNo
            """,
            mutableMapOf(
                "countNo" to countNo
            ),
            RowMapperImpl
        )

    /**
     * キーワードグループを検索します
     *
     * @param query クエリ
     */
    fun find(query: KeywordGroupQueryInput?): List<KeywordGroup> {
        val conditions = mutableListOf<String>()
        val params = mutableMapOf<String, Any>()

        if(query?.hasRecording == true) {
            conditions.add("""EXISTS(
                SELECT 1
                FROM foltia_subtitle AS S
                INNER JOIN foltia_keywordlibfiles AS F ON S.countno = F.countno
                WHERE S.tid = :tidKeyword AND F.keywordgroupid = W.keywordgroupid AND (
                    (S.m2pfilename IS NOT NULL AND EXISTS(SELECT 1 FROM foltia_m2pfiles AS TS WHERE TS.m2pfilename = S.m2pfilename)) OR
                    (S.pspfilename IS NOT NULL AND EXISTS(SELECT 1 FROM foltia_mp4files AS SD WHERE SD.mp4filename = S.pspfilename)) OR
                    (S.mp4hd IS NOT NULL AND EXISTS(SELECT 1 FROM foltia_hdmp4files AS HD WHERE HD.hdmp4filename = S.mp4hd))
                )
            )""".trimIndent())
            params["tidKeyword"] = Program.TID_KEYWORD
        } else if(query?.hasRecording == false) {
            conditions.add("""EXISTS(
                SELECT 1
                FROM foltia_subtitle AS S 
                INNER JOIN foltia_keywordlibfiles AS F ON S.countno = F.countno
                WHERE S.tid = :tidKeyword AND F.keywordgroupid = W.keywordgroupid AND (
                    S.m2pfilename IS NULL AND
                    S.pspfilename IS NULL AND
                    S.mp4hd IS NULL
                )
            )""".trimIndent())
            params["tidKeyword"] = Program.TID_KEYWORD
        }
        if(query?.videoTypes?.isNotEmpty() == true) {
            mutableListOf<String>()
                .also {
                    if(query.videoTypes.contains(VideoType.TS)) it.add("(S.m2pfilename IS NOT NULL AND EXISTS(SELECT 1 FROM foltia_m2pfiles AS TS WHERE TS.m2pfilename = S.m2pfilename))")
                    if(query.videoTypes.contains(VideoType.SD)) it.add("(S.pspfilename IS NOT NULL AND EXISTS(SELECT 1 FROM foltia_mp4files AS SD WHERE SD.mp4filename = S.pspfilename))")
                    if(query.videoTypes.contains(VideoType.HD)) it.add("(S.mp4hd IS NOT NULL AND EXISTS(SELECT 1 FROM foltia_hdmp4files AS HD WHERE HD.hdmp4filename = S.mp4hd))")
                }
                .joinToString(" OR ")
                .also {
                    conditions.add("""EXISTS(
                        SELECT 1
                        FROM foltia_subtitle AS S 
                        INNER JOIN foltia_keywordlibfiles AS F ON S.countno = F.countno
                        WHERE S.tid = :tidKeyword AND F.keywordgroupid = W.keywordgroupid AND (${it}) 
                    )""".trimIndent())
                }
            params["tidKeyword"] = Program.TID_KEYWORD
        }
        if(query?.keywordContains != null) {
            conditions.add("W.keyword LIKE :keywordContains")
            params["keywordContains"] = "%${query.keywordContains}%"
        }

        val where = if(conditions.isEmpty()) "" else "WHERE ${conditions.joinToString(" AND ")}"
        return jt.query(
            """
            SELECT
                W.*
            FROM
                foltia_keywordlibword AS W
            $where
            ORDER BY W.keywordgroupid
            """,
            params,
            RowMapperImpl
        )
    }

    /**
     * ResultSet から KeywordGroup にマッピングする RowMapper
     */
    private object RowMapperImpl : RowMapper<KeywordGroup> {
        override fun mapRow(rs: ResultSet, rowNum: Int): KeywordGroup =
            KeywordGroup(
                keywordGroupId = rs.getLong("keywordgroupid"),
                keyword = rs.getString("keyword"),
            )
    }
}
