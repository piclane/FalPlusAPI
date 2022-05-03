package com.xxuz.piclane.foltiaapi.dao

import com.xxuz.piclane.foltiaapi.model.Direction
import com.xxuz.piclane.foltiaapi.model.Program
import com.xxuz.piclane.foltiaapi.model.vo.SubtitleQueryInput
import com.xxuz.piclane.foltiaapi.model.Subtitle
import com.xxuz.piclane.foltiaapi.model.VideoType
import com.xxuz.piclane.foltiaapi.model.vo.SubtitleUpdateInput
import com.xxuz.piclane.foltiaapi.model.vo.SubtitleResult
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.Cacheable
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.sql.ResultSet

/**
 * 放送Dao
 */
@Repository
class SubtitleDao(
        @Autowired
        private val jt: NamedParameterJdbcTemplate,

        @Autowired
        private val cacheMgr: CacheManager,
) {
    /**
     * ID から放送を取得します
     */
    @Cacheable(cacheNames = ["foltia"], key = "'subtitle:pId=' + #pId")
    fun get(pId: Long): Subtitle? =
        try {
            jt.queryForObject(
                """
                SELECT
                    S.*,
                    N.recfilename
                FROM
                    foltia_subtitle AS S
                LEFT OUTER JOIN 
                    foltia_nowrecording AS N ON S.pid = N.pid
                WHERE
                    S.pid = :pId
                """,
                mutableMapOf(
                    "pId" to pId
                ),
                RowMapperImpl
            )
        } catch (e: EmptyResultDataAccessException) {
            null
        }

    /**
     * 放送を検索します
     *
     * @param query クエリ
     * @param offset 検索の先頭からのオフセット
     * @param limit 検索結果の最大取得件数
     * @param contextData 任意のコンテキストデータ
     */
    fun find(query: SubtitleQueryInput?, offset: Int, limit: Int, contextData: String? = null): SubtitleResult {
        val params = mutableMapOf<String, Any>(
            "offset" to offset,
            "limit" to limit,
        )
        val where = buildWhereClause(query, params).let { if(it.isBlank()) "" else "WHERE $it" }
        val direction = if(query?.direction == Direction.Ascending) "ASC" else "DESC"
        val data =  jt.query(
            """
            SELECT
                S.*,
                N.recfilename
            FROM
                foltia_subtitle AS S
            INNER JOIN
                foltia_program AS P ON S.tid = P.tid
            INNER JOIN
                foltia_station AS ST ON S.stationid = ST.stationid
            LEFT OUTER JOIN 
                foltia_nowrecording AS N ON S.pid = N.pid
            $where
            ORDER BY startdatetime $direction
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
                foltia_subtitle AS S
            INNER JOIN
                foltia_program AS P ON S.tid = P.tid
            INNER JOIN
                foltia_station AS ST ON S.stationid = ST.stationid
            LEFT OUTER JOIN 
                foltia_nowrecording AS N ON S.pid = N.pid
            $where
            """,
            params,
            Int::class.java
        )

        return SubtitleResult(offset, limit, contextData, total ?: 0, data)
    }

    /**
     * 指定されたクエリにおける、指定された放送のオフセットを取得します
     *
     * @param query クエリ
     * @param pId 放送 ID
     */
    fun getSubtitleOffset(query: SubtitleQueryInput?, pId: Long): Int? {
        val params = mutableMapOf<String, Any>(
            "pId" to pId
        )
        val where = buildWhereClause(query, params).let { if(it.isBlank()) "" else "WHERE $it" }
        val direction = if(query?.direction == Direction.Ascending) "ASC" else "DESC"
        return try {
            jt.queryForObject(
                """
                SELECT
                    X.ROW_NUM
                FROM
                    (SELECT
                        ROW_NUMBER() OVER (ORDER BY startdatetime $direction) - 1 AS ROW_NUM,
                        S.pid
                    FROM
                        foltia_subtitle AS S
                    INNER JOIN
                        foltia_program AS P ON S.tid = P.tid
                    INNER JOIN
                        foltia_station AS ST ON S.stationid = ST.stationid
                    LEFT OUTER JOIN 
                        foltia_nowrecording AS N ON S.pid = N.pid
                    $where) AS X
                WHERE
                    X.pid = :pId
                """,
                params,
                Int::class.java
            )
        } catch (e: EmptyResultDataAccessException) {
            null
        }
    }

    private fun buildWhereClause(query: SubtitleQueryInput?, params: MutableMap<String, Any>): String {
        val conditions = mutableListOf<String>()
        params["tIdKeyword"] = Program.TID_KEYWORD
        params["tIdEpg"] = Program.TID_EPG

        if(query?.tId != null) {
            conditions.add("S.tid = :tId")
            params["tId"] = query.tId
        }
        if(query?.recordingTypes != null && query.recordingTypes.isNotEmpty()) {
            query.recordingTypes
                .joinToString(" OR ") {
                    when (it) {
                        Subtitle.RecordingType.Program -> "S.tid > :tIdEpg"
                        Subtitle.RecordingType.Epg -> "S.tid = :tIdEpg"
                        Subtitle.RecordingType.Keyword -> "S.tid = :tIdKeyword"
                    }
                }
                .also { conditions.add(it) }
        }
        if(query?.receivableStation != null) {
            conditions.add("ST.receiving = :receivableStation")
            params["receivableStation"] = if(query.receivableStation) 1 else 0
        }
        if(query?.hasRecording == true) {
            conditions.add("""(
                ${if(query.nowRecording == true) "N.recfilename IS NOT NULL OR" else ""}
                (S.m2pfilename IS NOT NULL AND EXISTS(SELECT 1 FROM foltia_m2pfiles AS TS WHERE TS.m2pfilename = S.m2pfilename)) OR 
                (S.pspfilename IS NOT NULL AND EXISTS(SELECT 1 FROM foltia_mp4files AS SD WHERE SD.mp4filename = S.pspfilename)) OR 
                (S.mp4hd IS NOT NULL AND EXISTS(SELECT 1 FROM foltia_hdmp4files AS HD WHERE HD.hdmp4filename = S.mp4hd))
            )""".trimIndent())
        } else if(query?.hasRecording == false) {
            conditions.add("(S.m2pfilename IS NULL AND S.pspfilename IS NULL AND S.mp4hd IS NULL)")
        }
        if(query?.videoTypes?.isNotEmpty() == true) {
            mutableListOf<String>()
                .also {
                    if(query.nowRecording == true) it.add("N.recfilename IS NOT NULL")
                    if(query.videoTypes.contains(VideoType.TS)) it.add("(S.m2pfilename IS NOT NULL AND EXISTS(SELECT 1 FROM foltia_m2pfiles AS TS WHERE TS.m2pfilename = S.m2pfilename))")
                    if(query.videoTypes.contains(VideoType.SD)) it.add("(S.pspfilename IS NOT NULL AND EXISTS(SELECT 1 FROM foltia_mp4files AS SD WHERE SD.mp4filename = S.pspfilename))")
                    if(query.videoTypes.contains(VideoType.HD)) it.add("(S.mp4hd IS NOT NULL AND EXISTS(SELECT 1 FROM foltia_hdmp4files AS HD WHERE HD.hdmp4filename = S.mp4hd))")
                }
                .joinToString(" OR ", prefix = "(", postfix = ")")
                .also {
                    conditions.add(it)
                }
        }
        if(query?.keywordGroupId != null) {
            conditions.add("""
                S.tid = :tIdKeyword AND 
                EXISTS(
                    SELECT 1 
                    FROM foltia_keywordlibfiles AS F 
                    WHERE F.keywordgroupid = :keywordGroupId AND
                          F.countno = S.countno
                )
            """.trimIndent())
            params["keywordGroupId"] = query.keywordGroupId
        }
        if(query?.subtitleContains != null) {
            conditions.add("S.subtitle LIKE :subtitleContains")
            params["subtitleContains"] = "%${query.subtitleContains}%"
        }
        if(query?.fileStatuses != null && query.fileStatuses.isNotEmpty()) {
            conditions.add("S.filestatus IN (${query.fileStatuses.map { it.code }.joinToString(", ")})")
        }

        return conditions.joinToString(" AND ")
    }

    /**
     * 放送を更新します
     *
     * @param input 更新入力
     */
    fun update(input: SubtitleUpdateInput) {
        val sets = mutableListOf<String>()
        val params = mutableMapOf<String, Any>(
            "pId" to input.pId
        )

        if(input.subtitleDefined && input.subtitle?.isNotBlank() == true) {
            sets.add("subtitle = :subtitle")
            params["subtitle"] = input.subtitle
        }
        if(input.fileStatusDefined && input.fileStatus != null) {
            sets.add("filestatus = :fileStatus")
            params["fileStatus"] = input.fileStatus
        }
        if(input.encodeSettingDefined && input.encodeSetting != null) {
            sets.add("encodesetting = :encodeSetting")
            params["encodeSetting"] = input.encodeSetting
        }

        jt.update(
            """
            UPDATE
                foltia_subtitle
            SET
                ${sets.joinToString(", ")}
            WHERE
                pid = :pId
            """,
            params
        )

        // キャッシュの削除
        cacheMgr.getCache("foltia")?.evictIfPresent("subtitle:pId=${input.pId}")
    }

    /**
     * 動画ファイルを更新します
     *
     * @param pId 放送ID
     * @param videoType 動画ファイルの種別
     * @param filenameProvider ファイル名プロバイダ
     */
    fun updateVideo(pId: Long, videoType: VideoType, filenameProvider: (subtitle: Subtitle, videoType: VideoType) -> String): Subtitle? {
        val subtitle = get(pId) ?: return null
        val filename = filenameProvider(subtitle, videoType)

        // foltia_subtitle の列を更新
        val videoColumn = videoColumn(videoType)
        jt.update(
            """
            UPDATE foltia_subtitle SET $videoColumn = :filename WHERE pid = :pId
            """,
            mapOf(
                "pId" to pId,
                "filename" to filename
            )
        )

        // 各種動画ファイルテーブルの行を追加
        val videoTable = videoTable(videoType)
        if(videoType == VideoType.TS) {
            jt.update(
                """
                INSERT INTO ${videoTable.first}(${videoTable.second}) VALUES (:filename) ON CONFLICT DO NOTHING
                """,
                mapOf(
                    "filename" to filename
                )
            )
        } else {
            jt.update(
                """
                INSERT INTO ${videoTable.first}(tid, ${videoTable.second}) VALUES (:tId, :filename) ON CONFLICT DO NOTHING
                """,
                mapOf(
                    "tId" to subtitle.tId,
                    "filename" to filename
                )
            )
        }

        // キャッシュの削除
        cacheMgr.getCache("foltia")?.evictIfPresent("subtitle:pId=$pId")

        return get(pId)
    }

    /**
     * 動画ファイルを削除します
     *
     * @param pId 放送ID
     * @param videoTypes 動画ファイルの種別
     */
    fun deleteVideo(pId: Long, videoTypes: Set<VideoType>): Pair<Subtitle, Subtitle>? {
        val subtitle = get(pId) ?: return null

        // 各種動画ファイルテーブルの行を削除
        videoTypes.forEach { videoType ->
            val filename = subtitle.videoFilename(videoType) ?: return@forEach
            val videoTable = videoTable(videoType)
            jt.update(
                """
                DELETE FROM ${videoTable.first} WHERE ${videoTable.second} = :filename
                """,
                mapOf(
                    "filename" to filename
                )
            )
        }

        // キャッシュの削除
        cacheMgr.getCache("subtitle")?.evictIfPresent("subtitle:pId=$pId")

        return subtitle to (get(pId) ?: throw RuntimeException())
    }

    /**
     * 動画ファイルの種別から foltia_subtitle の列名を取得します
     */
    private fun videoColumn(videoType: VideoType) =
        when(videoType) {
            VideoType.TS -> "m2pfilename"
            VideoType.SD -> "pspfilename"
            VideoType.HD -> "mp4hd"
        }

    /**
     * 動画ファイルの種別から動画ファイルテーブルの表名と列名を取得します
     */
    private fun videoTable(videoType: VideoType) =
        when(videoType) {
            VideoType.TS -> "foltia_m2pfiles" to "m2pfilename"
            VideoType.SD -> "foltia_mp4files" to "mp4filename"
            VideoType.HD -> "foltia_hdmp4files" to "hdmp4filename"
        }

    /**
     * ResultSet から Subtitle にマッピングする RowMapper
     */
    private object RowMapperImpl : RowMapper<Subtitle> {
        override fun mapRow(rs: ResultSet, rowNum: Int): Subtitle {
            val recFilename = rs.getString("recfilename")
            val isRecording = recFilename?.isNotBlank() ?: false
            return Subtitle(
                pId = rs.getLong("pid"),
                tId = rs.getLong("tid"),
                stationId = rs.getLong("stationid"),
                countNo = rs.getLong("countno").let { if (rs.wasNull()) null else it },
                subtitle = rs.getString("subtitle"),
                startDateTime = rs.getLong("startdatetime").toLocalDateTime(),
                endDateTime = rs.getLong("enddatetime").toLocalDateTime(),
                startOffset = rs.getLong("startoffset"),
                lengthMin = rs.getLong("lengthmin"),
                m2pFilename = if(isRecording) recFilename else rs.getString("m2pfilename"),
                pspFilename = rs.getString("pspfilename"),
                epgAddedBy = rs.getLong("epgaddedby").let { if (rs.wasNull()) null else it },
                lastUpdate = rs.getTimestamp("lastupdate").toOffsetDateTime().orElse(null),
                fileStatus = rs.getInt("filestatus").let {
                    if (isRecording)
                        Subtitle.FileStatus.RECORDING
                    else if (rs.wasNull())
                        null
                    else
                        Subtitle.FileStatus.codeOf(it).orElseThrow()
                },
                aspect = rs.getInt("aspect").let { if (rs.wasNull()) null else it },
                encodeSetting = rs.getInt("encodesetting").let {
                    if (rs.wasNull())
                        null
                    else
                        Subtitle.TranscodeQuality.codeOf(it).orElseThrow()
                },
                mp4hd = rs.getString("mp4hd"),
                syobocalFlag = rs.getInt("syobocalflag").let {
                    if (rs.wasNull())
                        emptySet()
                    else
                        Subtitle.SyobocalFlag.codesOf(it)
                },
                syobocalRev = rs.getInt("syobocalrev"),
            )
        }
    }
}
