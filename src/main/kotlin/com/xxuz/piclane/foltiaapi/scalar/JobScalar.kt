package com.xxuz.piclane.foltiaapi.scalar

import graphql.schema.Coercing
import graphql.schema.GraphQLScalarType

/**
 * Job スカラ型
 */
val JobScalar = GraphQLScalarType.newScalar()
    .name("Job")
    .description("Job 型")
    .coercing(JobCoercing())
    .build()!!

/**
 * Job 用の制約インターフェイス
 */
private class JobCoercing : Coercing<String, String> {
    override fun parseValue(input: Any): String =
        input.toString()

    override fun parseLiteral(input: Any): String =
        input.toString()

    override fun serialize(dataFetcherResult: Any): String =
        dataFetcherResult.toString()
}
