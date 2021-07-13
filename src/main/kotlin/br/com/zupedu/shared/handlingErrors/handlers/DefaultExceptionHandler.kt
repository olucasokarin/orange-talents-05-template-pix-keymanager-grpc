package br.com.zupedu.shared.handlingErrors.handlers

import br.com.zupedu.shared.handlingErrors.ExceptionHandler
import br.com.zupedu.shared.handlingErrors.ExceptionHandler.StatusWithDetails
import io.grpc.Status

class DefaultExceptionHandler : ExceptionHandler<Exception> {

    override fun handle(e: Exception) : StatusWithDetails {
        val status = when (e) {
            is IllegalArgumentException -> Status.INVALID_ARGUMENT.withDescription(e.message)
            is IllegalStateException -> Status.FAILED_PRECONDITION.withDescription(e.message)
            else -> Status.UNKNOWN
        }
        return StatusWithDetails(status.withCause(e))
    }

    override fun supports(e: Exception) : Boolean = true
}
