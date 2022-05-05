package com.xxuz.piclane.foltiaapi.foltia

import com.xxuz.piclane.foltiaapi.util.pipeLog
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.io.File
import java.io.IOException

/**
 * makedlnastructure.pl コマンドのスタブ
 */
@Component
@Suppress("unused")
class MakeDlnaStructure(
    @Autowired
    private val config: FoltiaConfig,
) {
    /** ロガー */
    private val logger = LoggerFactory.getLogger(MakeDlnaStructure::class.java)

    /**
     * 全体をリビルドします
     */
    fun rebuild() {
        run("REBUILD")
    }

    /**
     * 指定されたファイルのシンボリックリンクを削除します
     */
    fun delete(file: File) {
        run(file.name, "DELETE")
    }

    /**
     * 指定されたファイルのシンボリックリンクを再生成します
     */
    fun exchange(file: File) {
        run(file.name, "EXCHANGE")
    }

    /**
     *  指定されたファイルのシンボリックリンクを新規に生成します
     */
    fun process(file: File) {
        run(file.name)
    }

    /**
     * makedlnastructure.pl を呼び出します
     *
     * @param commandArgs makedlnastructure.pl への引数
     */
    private fun run(vararg commandArgs: String) {
        val makeDlnaStructurePath = config.perlPath("makedlnastructure.pl")
        val exitCode = ProcessBuilder()
            .directory(config.perlToolPath)
            .command(makeDlnaStructurePath.absolutePath, *commandArgs)
            .redirectError(ProcessBuilder.Redirect.PIPE)
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .start()
            .pipeLog("[runMakeDlnaStructure] ", logger)
            .waitFor()
        if(exitCode != 0) {
            throw IOException("runMakeDlnaStructure Failed: makedlnastructure.pl ${commandArgs.joinToString(" ")}")
        }
    }
}
