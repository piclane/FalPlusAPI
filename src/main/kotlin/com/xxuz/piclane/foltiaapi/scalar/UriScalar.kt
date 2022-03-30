package com.xxuz.piclane.foltiaapi.scalar

import graphql.schema.Coercing
import graphql.schema.GraphQLScalarType
import java.net.URI

/**
 * URI スカラ型
 */
val UriScalar = GraphQLScalarType.newScalar()
    .name("URI")
    .description("URI 型")
    .coercing(URICoercing())
    .build()!!

/**
 * URI 用の制約インターフェイス
 */
private class URICoercing : Coercing<URI, String> {
    override fun parseValue(input: Any): URI =
        URI.create(input.toString())

    override fun parseLiteral(input: Any): URI =
        URI.create(input.toString())

    override fun serialize(dataFetcherResult: Any): String? =
        if (dataFetcherResult is URI) {
            dataFetcherResult.toString()
        } else {
            null
        }
}
