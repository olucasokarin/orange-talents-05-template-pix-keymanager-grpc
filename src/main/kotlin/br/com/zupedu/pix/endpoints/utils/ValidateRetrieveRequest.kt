package br.com.zupedu.pix.endpoints.utils

import br.com.zupedu.grpc.RetrievePixRequest
import br.com.zupedu.pix.RetrievePixService
import io.micronaut.validation.validator.Validator
import javax.validation.ConstraintViolationException

fun RetrievePixRequest.convertGrpcRequestToInternalRequest(validator: Validator): RetrievePixService {
    val retrievePixService = when (decideCase) {
        RetrievePixRequest.DecideCase.IDPIX -> idPix.run {
            RetrievePixService.RetrieveInternalAccess(idClient, idPix)
        }
        RetrievePixRequest.DecideCase.VALUEPIX -> RetrievePixService.RetrieveExternalAccess(valuePix)
        else -> RetrievePixService.InvalidValue()
    }

    val validate = validator.validate(retrievePixService)
    if (validate.isNotEmpty())
        throw ConstraintViolationException(validate)

    return retrievePixService
}
