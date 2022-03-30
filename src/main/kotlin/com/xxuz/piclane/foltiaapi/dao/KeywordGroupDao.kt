package com.xxuz.piclane.foltiaapi.dao

import com.xxuz.piclane.foltiaapi.model.KeywordGroup
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
