package com.xxuz.piclane.foltiaapi.job

import kotlinx.coroutines.*

/**
 * ジョブ
 */
interface Job {
    /** ジョブID */
    val jobId: String

    /** タスクをスケジュールします */
    fun schedule(block: suspend CoroutineScope.() -> Unit)

    /** タスクの実行を開始します */
    fun start()

    /** 進捗を 0 〜 1 の範囲で取得します */
    fun progress(): Float
}
