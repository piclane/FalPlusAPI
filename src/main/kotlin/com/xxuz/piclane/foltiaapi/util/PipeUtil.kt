package com.xxuz.piclane.foltiaapi.util

import org.slf4j.Logger
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.concurrent.Executors

private val es = Executors.newCachedThreadPool { r ->
    Thread(r).also {
        it.isDaemon = true
        it.priority = Thread.MIN_PRIORITY
    }
}

fun Process.pipeErrorLog(prefix: String, logger: Logger): Process {
    es.submit {
        Thread.currentThread().name = "stderr"
        BufferedReader(InputStreamReader(this.errorStream, StandardCharsets.UTF_8)).use { reader ->
            while (true) {
                val line = reader.readLine() ?: break
                logger.error(prefix + line)
                if(Thread.interrupted()) {
                    return@use
                }
            }
        }
    }
    return this
}

fun Process.pipeLog(prefix: String, logger: Logger): Process {
    es.submit {
        Thread.currentThread().name = "stdout"
        TerminalBufferedReader(InputStreamReader(this.inputStream, StandardCharsets.UTF_8)).use { reader ->
            while (true) {
                val line = reader.readLine() ?: break
                logger.info("[STDOUT] ${prefix}${line}")
                if(Thread.interrupted()) {
                    return@use
                }
            }
        }
    }
    es.submit {
        Thread.currentThread().name = "stderr"
        TerminalBufferedReader(InputStreamReader(this.errorStream, StandardCharsets.UTF_8)).use { reader ->
            while (true) {
                val line = reader.readLine() ?: break
                logger.info("[STDERR] ${prefix}${line}")
                if(Thread.interrupted()) {
                    return@use
                }
            }
        }
    }
    return this
}

