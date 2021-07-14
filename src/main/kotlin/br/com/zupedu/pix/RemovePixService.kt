package br.com.zupedu.pix

import br.com.zupedu.PixKeyRemoveRequest
import br.com.zupedu.pix.externalConnections.itau.ClientItau
import br.com.zupedu.pix.repository.PixRepository
import br.com.zupedu.shared.handlingErrors.exceptions.KeyPixNotExistingException
import io.micronaut.http.HttpStatus
import io.micronaut.validation.Validated
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Valid

@Validated
@Transactional
@Singleton
class RemovePixService(
    @Inject val pixRepository: PixRepository,
    @Inject val clientItau: ClientItau
) {

    fun remove(@Valid request: PixKeyRemoveRequest) {

        val optionalPixKey = pixRepository.findByExternalId(UUID.fromString(request.idPixKey))
        if (optionalPixKey.isEmpty)
            throw KeyPixNotExistingException("Key Pix '${request.idPixKey}' not existing")

        val response = clientItau.retrieveAccountClient(request.idClient)
        if (response.status == HttpStatus.NOT_FOUND)
            throw IllegalStateException("Account not found")

        val pixKey = optionalPixKey.get()
        if(!pixKey.pixBelongsToTheClient(request.idClient))
            throw IllegalStateException("This pix key not belongs to this client")

        pixRepository.deleteById(pixKey.id)
    }
}
