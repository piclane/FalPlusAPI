package com.xxuz.piclane.foltiaapi

import graphql.GraphQLError
import graphql.GraphQLException
import graphql.kickstart.execution.error.GenericGraphQLError
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.ExceptionHandler

/**
 * 例外ハンドラ
 */
@Component
class ExceptionHandler {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(ExceptionHandler::class.java)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(e: IllegalArgumentException): GraphQLError {
        logger.debug(e.message, e)
        return GenericGraphQLError("IllegalArgumentException: ${e.message}")
    }

    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalStateException(e: IllegalStateException): GraphQLError {
        logger.error(e.message, e)
        return GenericGraphQLError("IllegalStateException: ${e.message}")
    }

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNoSuchElementException(e: NoSuchElementException): GraphQLError {
        logger.debug(e.message, e)
        return GenericGraphQLError("NoSuchElementException: ${e.message}")
    }

    @ExceptionHandler(SecurityException::class)
    fun handleSecurityException(e: SecurityException): GraphQLError {
        logger.error(e.message, e)
        return GenericGraphQLError("SecurityException: ${e.message}")
    }

    @ExceptionHandler(GraphQLException::class)
    fun handleGraphQLException(e: GraphQLException): GraphQLError {
        if(logger.isDebugEnabled) {
            logger.debug("${e.javaClass.name}: ${e.message}")
        }
        return GenericGraphQLError("${e.javaClass.simpleName}: ${e.message}")
    }
}
