package com.xxuz.piclane.foltiaapi.scalar

import graphql.schema.Coercing
import graphql.schema.GraphQLScalarType
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

/**
 * OffsetDateTime スカラ型
 */
val OffsetDateTimeScalar = GraphQLScalarType.newScalar()
    .name("OffsetDateTime")
    .description("ISO-8601形式のオフセット付き日時型")
    .coercing(OffsetDateTimeCoercing())
    .build()!!

/**
 * OffsetDateTime 用の制約インターフェイス
 */
private class OffsetDateTimeCoercing : Coercing<OffsetDateTime, String> {
    override fun parseValue(input: Any): OffsetDateTime =
        OffsetDateTime.parse(input.toString(), DateTimeFormatter.ISO_OFFSET_DATE_TIME)

    override fun parseLiteral(input: Any): OffsetDateTime =
        OffsetDateTime.parse(input.toString(), DateTimeFormatter.ISO_OFFSET_DATE_TIME)

    override fun serialize(dataFetcherResult: Any): String? =
        if (dataFetcherResult is OffsetDateTime) {
            dataFetcherResult.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        } else {
            null
        }
}
