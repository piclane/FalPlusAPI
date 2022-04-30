package com.xxuz.piclane.foltiaapi.job

import kotlinx.coroutines.*
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * ジョブマネージャー
 */
@Component
class JobManager {
    /** ジョブ ID とジョブのリスト */
    private val runningJobs = ConcurrentHashMap<String, Job>()

    /**
     * ジョブを生成します
     *
     * @param context ExecutorCoroutineDispatcher
     */
    fun issue(context: ExecutorCoroutineDispatcher): Job =
        JobImpl(context)

    /**
     * ジョブを取得します
     */
    fun get(jobId: String): Job? =
        runningJobs[jobId]

    /**
     * ジョブの実装
     */
    private inner class JobImpl(
        private val context: ExecutorCoroutineDispatcher
    ): Job {
        /** スケジュールされたタスク */
        private val schedules = mutableListOf<suspend CoroutineScope.() -> Unit>()

        /** 実行中のジョブ */
        private val jobs = mutableListOf<kotlinx.coroutines.Job>()

        /** ジョブID */
        override val jobId: String = UUID.randomUUID().toString()

        override fun schedule(block: suspend CoroutineScope.() -> Unit) {
            schedules.add(block)
        }

        override fun start() {
            if(schedules.isEmpty()) {
                return
            }
            schedules.forEach {
                jobs.add(GlobalScope.launch(context, start = CoroutineStart.DEFAULT, block = it))
            }
            GlobalScope.launch {
                jobs.forEach { it.join() }
                runningJobs.remove(jobId)
            }
            runningJobs[jobId] = this
        }

        override fun progress(): Float =
            jobs.count { it.isCompleted }.toFloat() / jobs.size.toFloat()
    }

}
