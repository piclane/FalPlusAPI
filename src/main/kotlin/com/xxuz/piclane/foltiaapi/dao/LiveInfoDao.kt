package com.xxuz.piclane.foltiaapi.dao

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository

/**
 * LiveInfo
 */
@Repository
class LiveInfoDao(
    @Autowired
    private val jt: NamedParameterJdbcTemplate
)  {
    /**
     * 全てのライブIDを取得します
     */
    fun findLiveIds(): List<String> =
            jt.query(
                """SELECT m3u8name FROM foltia_liveinfo""",
                mutableMapOf<String, Any>()
            ) { rs, _ ->
                rs.getString("m3u8name")
            }
            .filterNotNull()
}
