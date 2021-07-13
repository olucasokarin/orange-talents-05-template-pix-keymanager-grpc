package br.com.zupedu.shared.handlingErrors

import br.com.zupedu.shared.handlingErrors.handlers.DefaultExceptionHandler
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ErrorAroundHandlerResolver(
    @Inject private val handlers: List<ExceptionHandler<Exception>>,
) {
    private var defaultHandler: ExceptionHandler<Exception> = DefaultExceptionHandler();

    constructor(handlers: List<ExceptionHandler<Exception>>, defaultHandler: ExceptionHandler<Exception>) : this(handlers) {
        this.defaultHandler = defaultHandler
    }

    fun resolve(ex: Exception) : ExceptionHandler<*> {
        val foundHandlers = handlers.filter { it.supports(ex) }

        if (foundHandlers.size > 1)
            throw IllegalStateException("Too many handlers supporting the same exception '${ex.javaClass.name}': $foundHandlers")

        return foundHandlers.firstOrNull() ?: defaultHandler
    }
}
