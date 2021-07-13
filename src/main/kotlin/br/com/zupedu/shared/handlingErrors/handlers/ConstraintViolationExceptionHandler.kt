package br.com.zupedu.shared.handlingErrors.handlers

import br.com.zupedu.shared.handlingErrors.ExceptionHandler
import br.com.zupedu.shared.handlingErrors.ExceptionHandler.StatusWithDetails
import com.google.protobuf.Any
import com.google.rpc.BadRequest
import com.google.rpc.Code
import com.google.rpc.Status
import javax.inject.Singleton
import javax.validation.ConstraintViolationException


/**
 * Handles the Bean Validation errors adding theirs violations into request trailers (metadata)
 */
@Singleton
class ConstraintViolationExceptionHandler: ExceptionHandler<ConstraintViolationException> {
    override fun handle(e: ConstraintViolationException): StatusWithDetails {

        val details = BadRequest.newBuilder()
            .addAllFieldViolations(e.constraintViolations.map {
                BadRequest.FieldViolation.newBuilder()
                    .setField(it.propertyPath.last().name ?: "?? key ??")
                    .setDescription(it.message)
                    .build()
            })
            .build()

        val statusProto = Status.newBuilder()
            .setCode(Code.INVALID_ARGUMENT_VALUE)
            .setMessage("Invalid Data")
            .addDetails(Any.pack(details))
            .build()

        return StatusWithDetails(statusProto)
    }

    override fun supports(e: Exception): Boolean =
        e is ConstraintViolationException
}
