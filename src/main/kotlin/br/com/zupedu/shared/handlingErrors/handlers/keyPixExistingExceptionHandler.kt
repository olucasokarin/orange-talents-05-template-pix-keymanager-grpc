package br.com.zupedu.shared.handlingErrors.handlers

import br.com.zupedu.shared.handlingErrors.ExceptionHandler
import br.com.zupedu.shared.handlingErrors.exceptions.KeyPixExistingException
import io.grpc.Status
import javax.inject.Singleton

@Singleton
class keyPixExistingExceptionHandler : ExceptionHandler<KeyPixExistingException> {
    override fun handle(e: KeyPixExistingException): ExceptionHandler.StatusWithDetails =
        ExceptionHandler.StatusWithDetails(Status.ALREADY_EXISTS
            .withDescription(e.message)
            .withCause(e))

    override fun supports(e: Exception): Boolean =
        e is KeyPixExistingException
}
