package com.xxuz.piclane.foltiaapi.scalar

import graphql.language.IntValue
import graphql.schema.Coercing
import graphql.schema.GraphQLScalarType
import java.math.BigInteger

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
private class LongCoercing : Coercing<Long, Long> {
    override fun parseValue(input: Any): Long =
        if(input is IntValue)
            input.value.toLong()
        else
            input.toString().toLong()

    override fun parseLiteral(input: Any): Long =
        if(input is IntValue)
            input.value.toLong()
        else
            input.toString().toLong()

    override fun serialize(dataFetcherResult: Any): Long? =
        if (dataFetcherResult is Long) {
            dataFetcherResult
        } else {
            null
        }
}
