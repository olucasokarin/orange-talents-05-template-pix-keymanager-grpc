package br.com.zupedu.shared.handlingErrors.handlers

import br.com.zupedu.shared.handlingErrors.ExceptionHandler
import br.com.zupedu.shared.handlingErrors.ExceptionHandler.StatusWithDetails
import br.com.zupedu.shared.handlingErrors.exceptions.KeyPixNotExistingException
import io.grpc.Status
import javax.inject.Singleton

@Singleton
class KeyPixNotExistingExceptionHandler : ExceptionHandler<KeyPixNotExistingException> {
    override fun handle(e: KeyPixNotExistingException): StatusWithDetails =
        StatusWithDetails(Status.NOT_FOUND
            .withDescription(e.message)
            .withCause(e))

    override fun supports(e: Exception): Boolean =
        e is KeyPixNotExistingException
}
