package com.xxuz.piclane.foltiaapi.scalar

import graphql.schema.Coercing
import graphql.schema.GraphQLScalarType
import java.time.Duration

/**
 * Duration スカラ型
 */
val DurationScalar = GraphQLScalarType.newScalar()
    .name("Duration")
    .description("ISO-8601秒ベース表現を使用したデュレーション型")
    .coercing(DurationCoercing())
    .build()!!

/**
 * Duration 用の制約インターフェイス
 */
private class DurationCoercing : Coercing<Duration, String> {
    override fun parseValue(input: Any): Duration =
        Duration.parse(input.toString())

    override fun parseLiteral(input: Any): Duration =
        Duration.parse(input.toString())

    override fun serialize(dataFetcherResult: Any): String? =
        if (dataFetcherResult is Duration) {
            dataFetcherResult.toString()
        } else {
            null
        }
}
