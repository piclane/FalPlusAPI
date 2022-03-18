package com.xxuz.piclane.foltiaapi.util

import java.io.*

/**
 * ストリームの為のユーティリティー
 *
 * @author yohei_hina
 */
object StreamUtil {
    private const val BUFFER_SIZE = 3000

    /**
     * 入力ストリームから出力ストリームに吸い上げます
     *
     * @param in 入力ストリーム
     * @param out 出力ストリーム
     */
    @Throws(IOException::class)
    fun pumpStream(`in`: InputStream, out: OutputStream) {
        val buf = ByteArray(BUFFER_SIZE)
        var len: Int
        while (`in`.read(buf, 0, BUFFER_SIZE).also { len = it } != -1) {
            out.write(buf, 0, len)
        }
    }

    /**
     * リーダーからライターに吸い上げます
     *
     * @param reader リーダー
     * @param writer ライター
     */
    @Throws(IOException::class)
    fun pumpStream(reader: Reader, writer: Writer) {
        val buf = CharArray(BUFFER_SIZE)
        var len: Int
        while (reader.read(buf, 0, BUFFER_SIZE).also { len = it } != -1) {
            writer.write(buf, 0, len)
        }
    }
}
