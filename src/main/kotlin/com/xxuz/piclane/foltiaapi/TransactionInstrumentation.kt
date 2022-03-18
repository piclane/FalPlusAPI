package com.xxuz.piclane.foltiaapi

import graphql.ExecutionResult
import graphql.execution.instrumentation.InstrumentationContext
import graphql.execution.instrumentation.SimpleInstrumentation
import graphql.execution.instrumentation.SimpleInstrumentationContext
import graphql.execution.instrumentation.parameters.InstrumentationExecuteOperationParameters
import graphql.language.OperationDefinition
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate

/**
 * リクエスト毎にトランザクションの開始・終了を行う Graphql 観測クラス
 */
@Component
class TransactionInstrumentation : SimpleInstrumentation() {
    @Autowired
    private lateinit var tm: PlatformTransactionManager

    /**
     * @see graphql.execution.instrumentation.SimpleInstrumentation.beginExecuteOperation
     */
    override fun beginExecuteOperation(parameters: InstrumentationExecuteOperationParameters): InstrumentationContext<ExecutionResult> {
        val tx = TransactionTemplate(tm)
        val op = parameters.executionContext.operationDefinition.operation
        if (OperationDefinition.Operation.MUTATION != op) {
            tx.isReadOnly = true
        }
        val status = tm.getTransaction(tx)
        return SimpleInstrumentationContext.whenDispatched { codeToRun ->
            val result = codeToRun.join()
            if (codeToRun.isCompletedExceptionally || result.errors.isNotEmpty()) {
                tm.rollback(status)
            } else {
                tm.commit(status)
            }
        }
    }
}
