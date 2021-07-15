package br.com.zupedu.pix

import br.com.zupedu.KeyPixRequest
import br.com.zupedu.pix.externalConnections.bcb.ClientBcb
import br.com.zupedu.pix.externalConnections.bcb.requests.CreatePixKeyRequest
import br.com.zupedu.pix.externalConnections.itau.ClientItau
import br.com.zupedu.pix.model.KeyPix
import br.com.zupedu.pix.model.enums.TypeAccount
import br.com.zupedu.pix.repository.PixRepository
import br.com.zupedu.shared.handlingErrors.exceptions.KeyPixExistingException
import io.micronaut.http.HttpStatus
import io.micronaut.validation.Validated
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Valid

@Singleton
@Validated
@Transactional
class RegisterPixService(
    @Inject val pixRepository: PixRepository,
    @Inject val clientItau: ClientItau,
    @Inject val clientBcb: ClientBcb,
) {

    fun register(@Valid newKeyPix: KeyPixRequest): KeyPix {

        if (pixRepository.existsByValueKey(newKeyPix.valueKey))
            throw KeyPixExistingException("Key Pix '${newKeyPix.valueKey}' existing")

        val accountFormatted =
            convertEnumInternalToEnumExternalConnection(newKeyPix.typeAccount)
        val response = clientItau.retrieve(newKeyPix.idClient, accountFormatted)

        if(response.status() == HttpStatus.NOT_FOUND)
            throw IllegalStateException("Client or account not found")

        val keyPix = newKeyPix.convertToEntityPix(response.body()!!)
        val createPixRequest = CreatePixKeyRequest.receiveEntityToPixRequest(keyPix)

        val responseKeyBcb = clientBcb.registerKey(createPixRequest)
        if (responseKeyBcb.status == HttpStatus.UNPROCESSABLE_ENTITY)
            throw IllegalStateException("Pix key already exist on  BCB")

        keyPix.updateValueKey(responseKeyBcb.body().key)
        pixRepository.save(keyPix)

        return keyPix
    }
}

fun convertEnumInternalToEnumExternalConnection(value: TypeAccount?) =
     when (value) {
        TypeAccount.CHECKING_ACCOUNT -> "CONTA_CORRENTE"
        else -> "CONTA_POUPANCA"
}
