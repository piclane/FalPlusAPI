package com.xxuz.piclane.foltiaapi

import com.xxuz.piclane.foltiaapi.scalar.LocalDateScalar
import com.xxuz.piclane.foltiaapi.scalar.LocalDateTimeScalar
import com.xxuz.piclane.foltiaapi.scalar.OffsetDateTimeScalar
import com.xxuz.piclane.foltiaapi.scalar.VoidScalar
import graphql.kickstart.servlet.apollo.ApolloScalars
import graphql.kickstart.tools.SchemaParserDictionary
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean

@SpringBootApplication
@EnableCaching
@ConfigurationPropertiesScan
class MainApplication {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplication.run(MainApplication::class.java, *args)
        }
    }

    @Bean
    fun getSchemaParser(): SchemaParserDictionary =
        SchemaParserDictionary().apply {
//            add(ApplicationComponentModel::class)
        }

    @Bean
    fun getVoid() = VoidScalar

    @Bean
    fun getOffsetDateTimeScalar() = OffsetDateTimeScalar

    @Bean
    fun getLocalDateTimeScalar() = LocalDateTimeScalar

    @Bean
    fun getLocalDateScalar() = LocalDateScalar

    @Bean
    fun getUploadScalar() = ApolloScalars.Upload
}
