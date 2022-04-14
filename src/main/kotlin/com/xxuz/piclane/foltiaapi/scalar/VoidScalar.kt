package com.xxuz.piclane.foltiaapi.scalar

import graphql.schema.Coercing
import graphql.schema.GraphQLScalarType

/**
 * Void スカラ型
 */
val VoidScalar = GraphQLScalarType.newScalar()
        .name("Void")
        .description("Void 型")
        .coercing(VoidCoercing())
        .build()!!

/**
 * Void 用の制約インターフェイス
 */
private class VoidCoercing: Coercing<Unit, Unit> {
    override fun parseValue(input: Any): Unit = Unit
    override fun parseLiteral(input: Any): Unit = Unit
    override fun serialize(dataFetcherResult: Any): Unit = Unit
}
