package com.xxuz.piclane.foltiaapi.util

import org.slf4j.Logger
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicReference

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
                logger.info("${prefix}[STDOUT] ${line.trimEnd()}")
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
                logger.info("${prefix}[STDERR] ${line.trimEnd()}")
                if(Thread.interrupted()) {
                    return@use
                }
            }
        }
    }
    return this
}

fun Process.readFirstLine(ref: AtomicReference<String>): Process {
    es.submit {
        Thread.currentThread().name = "stdout"
        TerminalBufferedReader(InputStreamReader(this.inputStream, StandardCharsets.UTF_8)).use { reader ->
            ref.set(reader.readLine()?.trimEnd() ?: "")
        }
    }
    return this
}

