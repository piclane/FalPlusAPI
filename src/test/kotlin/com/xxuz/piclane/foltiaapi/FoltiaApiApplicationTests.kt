package com.xxuz.piclane.foltiaapi

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(
    classes = [ MainApplication::class ],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
)
class FoltiaApiApplicationTests {

    @Test
    fun contextLoads() {
    }

}
