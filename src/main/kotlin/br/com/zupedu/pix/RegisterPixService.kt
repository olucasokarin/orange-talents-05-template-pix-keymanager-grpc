package br.com.zupedu.pix

import br.com.zupedu.KeyPixRequest
import br.com.zupedu.pix.model.KeyPix
import br.com.zupedu.pix.repository.PixRepository
import br.com.zupedu.shared.handlingErrors.exceptions.KeyPixExistingException
import io.micronaut.validation.Validated
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Valid

@Singleton
@Validated
@Transactional
class RegisterPixService(
    @Inject val pixRepository: PixRepository
) {

    fun register(@Valid newKeyPix: KeyPixRequest): KeyPix {

        if (pixRepository.existsByValueKey(newKeyPix.valueKey))
            throw KeyPixExistingException("Key Pix '${newKeyPix.valueKey}' existing")
        println(newKeyPix)

        val keyPix = newKeyPix.convertToEntityPix()

        pixRepository.save(keyPix)

        return keyPix;
    }
}
