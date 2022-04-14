package com.xxuz.piclane.foltiaapi.scalar

import graphql.schema.Coercing
import graphql.schema.GraphQLScalarType
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

/**
 * LocalDateTime スカラ型
 */
val LocalDateTimeScalar = GraphQLScalarType.newScalar()
    .name("LocalDateTime")
    .description("ISO-8601形式の日時型")
    .coercing(LocalDateTimeCoercing())
    .build()!!

/**
 * LocalDateTime 用の制約インターフェイス
 */
private class LocalDateTimeCoercing : Coercing<LocalDateTime, String> {
    override fun parseValue(input: Any): LocalDateTime =
        LocalDateTime.parse(input.toString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME)

    override fun parseLiteral(input: Any): LocalDateTime =
        LocalDateTime.parse(input.toString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME)

    override fun serialize(dataFetcherResult: Any): String? =
        if (dataFetcherResult is LocalDateTime) {
            dataFetcherResult.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        } else {
            null
        }
}
