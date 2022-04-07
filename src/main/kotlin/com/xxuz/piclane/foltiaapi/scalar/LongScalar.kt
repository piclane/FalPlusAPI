package com.xxuz.piclane.foltiaapi.scalar

import graphql.schema.Coercing
import graphql.schema.GraphQLScalarType

/**
 * Long スカラ型
 */
val LongScalar = GraphQLScalarType.newScalar()
    .name("Long")
    .description("Long 型")
    .coercing(LongCoercing())
    .build()!!

/**
 * Long 用の制約インターフェイス
 */
private class LongCoercing : Coercing<Long, String> {
    override fun parseValue(input: Any): Long =
        input.toString().toLong()

    override fun parseLiteral(input: Any): Long =
        input.toString().toLong()

    override fun serialize(dataFetcherResult: Any): String? =
        if (dataFetcherResult is Long) {
            dataFetcherResult.toString()
        } else {
            null
        }
}
