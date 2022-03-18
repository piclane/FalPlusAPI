package com.xxuz.piclane.foltiaapi.foltia

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.io.File
import java.net.URI

@Configuration
class FoltiaConfiguration(
    @Autowired
    val jt: NamedParameterJdbcTemplate
) {
    @Bean
    fun foltiaConfig(): FoltiaConfig {
        val result = mutableMapOf<String, String>()
        jt.query(
            "SELECT * FROM foltia_config"
        ) { rs ->
            result[rs.getString("key")] = rs.getString("value")
        }
        return FoltiaConfig(
            perlToolPath = result["perltoolpath"]?.let { File(it) } ?: throw IllegalStateException("The perltoolpath configuration is missing."),
            phpToolPath = result["phptoolpath"]?.let { File(it) } ?: throw IllegalStateException("The phptoolpath configuration is missing."),
            recFolderPath = result["recfolderpath"]?.let { File(it) } ?: throw IllegalStateException("The recfolderpath configuration is missing."),
            httpMediaMapPath = result["httpmediamappath"]?.let { URI(it) } ?: throw IllegalStateException("The httpmediamappath configuration is missing."),
            firmwareVersion = result["firmwareversion"] ?: "",
        )
    }
}
