package com.xxuz.piclane.foltiaapi.util

import java.io.BufferedReader
import java.io.Reader

/**
 * ターミナル用リーダー
 */
class TerminalBufferedReader(reader: Reader): BufferedReader(reader) {
    private var nextCode: Int? = null

    private var isEof = false

    /**
     * 一行読み取ります
     * 文字列の末尾には改行コードが付加されたものが返ります
     */
    override fun readLine(): String? {
        if(isEof) {
            return null
        }

        var buf: StringBuilder? = null
        while(true) {
            val i1 = if(nextCode != null) {
                val r = nextCode; nextCode = null
                r!!
            } else {
                read()
            }
            if (i1 == -1) {
                isEof = true
                return buf?.toString()
            }
            if (buf == null) {
                buf = StringBuilder()
            }
            val c1 = i1.toChar()
            buf.append(c1)
            if(c1 == '\n') {
                return buf.toString()
            }
            if(c1 == '\r') {
                val i2 = read()
                if(i2 == -1) {
                    isEof = true
                    return buf.toString()
                }
                val c2  = i2.toChar()
                if(c2 == '\n') {
                    buf.append(c2)
                } else {
                    nextCode = i2
                }
                return buf.toString()
            }
        }
    }
}
