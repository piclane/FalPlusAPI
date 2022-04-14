package com.xxuz.piclane.foltiaapi.dao

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import java.sql.Timestamp
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.zone.ZoneRules
import java.util.*

/** このサーバーの所属するタイムゾーンのオフセットルール */
private val zoneRules: ZoneRules = ZoneId.systemDefault().rules

private val simpleDateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm")

/**
 * Timestamp を null 安全に OffsetDateTime に変換します
 */
fun Timestamp?.toOffsetDateTime(): Optional<OffsetDateTime> {
    if(this == null) {
        return Optional.empty()
    }

    return Optional.of(
        OffsetDateTime.of(
            this.toLocalDateTime(),
            zoneRules.getOffset(Instant.now())))
}

/**
 * OffsetDateTime を null 安全に Timestamp に変換します
 */
fun OffsetDateTime?.toTimestamp(): Optional<Timestamp> {
    if(this == null) {
        return Optional.empty()
    }

    return Optional.of(
        Timestamp.from(this.toInstant())
    )
}

fun Long.toLocalDateTime(): LocalDateTime =
        LocalDateTime.parse(this.toString(10), simpleDateTimeFormatter)
