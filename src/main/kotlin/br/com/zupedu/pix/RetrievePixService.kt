package br.com.zupedu.pix

import br.com.zupedu.pix.externalConnections.bcb.ClientBcb
import br.com.zupedu.pix.model.KeyPix
import br.com.zupedu.pix.repository.PixRepository
import br.com.zupedu.shared.handlingErrors.exceptions.KeyPixNotExistingException
import io.micronaut.core.annotation.Introspected
import io.micronaut.http.HttpStatus
import io.micronaut.validation.Validated
import java.lang.IllegalArgumentException
import java.util.*
import javax.inject.Singleton
import javax.validation.constraints.NotBlank

@Singleton
@Validated
sealed class RetrievePixService {

    abstract fun filter(pixRepository: PixRepository, clientBcb: ClientBcb): KeyPix

    @Introspected
    data class RetrieveInternalAccess(
        @field:NotBlank
        val idClient: String,
        @field:NotBlank
        val idPix: String
    ) : RetrievePixService() {
        override fun filter(pixRepository: PixRepository, clientBcb: ClientBcb): KeyPix {
            val optionalPix = pixRepository.findByIdClient(UUID.fromString(idClient), UUID.fromString(idPix))

            if (optionalPix.isEmpty)
                throw KeyPixNotExistingException("Pix not found")

            val responseBcb = clientBcb.retrieveKey(optionalPix.get().valueKey)
            if (responseBcb.status == HttpStatus.NOT_FOUND)
                throw KeyPixNotExistingException("Pix not found")

            return optionalPix.get()
        }
    }

    @Introspected
    data class RetrieveExternalAccess(
        @field:NotBlank
        val valueKey: String
    ): RetrievePixService() {
        override fun filter(pixRepository: PixRepository, clientBcb: ClientBcb): KeyPix {
            val optionalPix = pixRepository.findByValueKey(valueKey)

            val responseBcb = clientBcb.retrieveKey(valueKey)
            if (optionalPix.isEmpty || responseBcb.status == HttpStatus.NOT_FOUND)
                throw KeyPixNotExistingException("Pix not found")

            return optionalPix.get()
        }
    }

    class InvalidValue() : RetrievePixService() {
        override fun filter(pixRepository: PixRepository, clientBcb: ClientBcb): KeyPix =
            throw IllegalArgumentException("Invalid data informed")
    }
}
