package com.xxuz.piclane.foltiaapi.scalar

import graphql.schema.Coercing
import graphql.schema.GraphQLScalarType
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

/**
 * LocalDate スカラ型
 */
val LocalDateScalar = GraphQLScalarType.newScalar()
    .name("LocalDate")
    .description("ISO-8601形式の日付型")
    .coercing(LocalDateCoercing())
    .build()!!

/**
 * LocalDate 用の制約インターフェイス
 */
private class LocalDateCoercing : Coercing<LocalDate, String> {
    override fun parseValue(input: Any): LocalDate =
        LocalDate.parse(input.toString(), DateTimeFormatter.ISO_LOCAL_DATE)

    override fun parseLiteral(input: Any): LocalDate =
        LocalDate.parse(input.toString(), DateTimeFormatter.ISO_LOCAL_DATE)

    override fun serialize(dataFetcherResult: Any): String? =
        if (dataFetcherResult is LocalDate) {
            dataFetcherResult.format(DateTimeFormatter.ISO_LOCAL_DATE)
        } else {
            null
        }
}
