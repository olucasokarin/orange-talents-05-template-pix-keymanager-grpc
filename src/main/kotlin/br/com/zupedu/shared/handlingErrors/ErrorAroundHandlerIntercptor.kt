package br.com.zupedu.shared.handlers

import br.com.zupedu.shared.handlingErrors.ErrorAroundHandler
import br.com.zupedu.shared.handlingErrors.ErrorAroundHandlerResolver
import br.com.zupedu.shared.handlingErrors.ExceptionHandler
import io.grpc.stub.StreamObserver
import io.micronaut.aop.InterceptorBean
import io.micronaut.aop.MethodInterceptor
import io.micronaut.aop.MethodInvocationContext
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.Exception

@Singleton
@InterceptorBean(ErrorAroundHandler::class)
class ErrorAroundHandlerInterceptor(
    @Inject private val resolver: ErrorAroundHandlerResolver
) : MethodInterceptor<Any, Any> {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun intercept(context: MethodInvocationContext<Any, Any>): Any? {

        return try {
            context.proceed()
        } catch (e: Exception) {

            logger.error("Handling the exception '${e.javaClass.name}' while processing the call: ${context.targetMethod}", e)

            @Suppress("UNCHECKED_CAST")
            val handler = resolver.resolve(e) as ExceptionHandler<Exception>
            val status = handler.handle(e)

            GrpcEndpointArguments(context).response()
                .onError(status.asRuntimeException())

            null
        }
    }
}

    private class GrpcEndpointArguments(val context: MethodInvocationContext<Any, Any>) {
        fun response() = context.parameterValues[1] as StreamObserver<*>
  }
