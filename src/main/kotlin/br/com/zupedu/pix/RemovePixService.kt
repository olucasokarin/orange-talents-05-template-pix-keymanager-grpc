package br.com.zupedu.pix

import br.com.zupedu.PixKeyRemoveRequest
import br.com.zupedu.pix.externalConnections.bcb.ClientBcb
import br.com.zupedu.pix.externalConnections.bcb.requests.DeletePixKeyRequest
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
    @Inject val clientItau: ClientItau,
    @Inject val clientBcb: ClientBcb,
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

        val deletePixKeyRequest = DeletePixKeyRequest(pixKey.valueKey, pixKey.institution.ispb)
        val responseKeyBcb = clientBcb.removeKey(pixKey.valueKey, deletePixKeyRequest)

        if (responseKeyBcb.status == HttpStatus.NOT_FOUND)
            pixRepository.deleteById(pixKey.id!!)
//            throw IllegalStateException("This pix not found on system BCB")

        if (responseKeyBcb.status == HttpStatus.FORBIDDEN)
            throw IllegalStateException("Participant is not allowed to access this resource")

        pixRepository.deleteById(pixKey.id!!)
    }
}
