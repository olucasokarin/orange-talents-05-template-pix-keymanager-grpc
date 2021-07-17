package br.com.zupedu.pix

import br.com.zupedu.pix.endpoints.RetrieveAllKeyPixRequest
import br.com.zupedu.pix.externalConnections.itau.ClientItau
import br.com.zupedu.pix.model.KeyPix
import br.com.zupedu.pix.repository.PixRepository
import io.micronaut.http.HttpStatus
import io.micronaut.validation.Validated
import java.util.*
import javax.inject.Inject
import javax.transaction.Transactional
import javax.validation.Valid

@Validated
@Transactional
class RetrieveListService(
    @Inject val pixRepository: PixRepository,
    @Inject val clientItau: ClientItau,
) {
    fun retrieveAllPix(@Valid request: RetrieveAllKeyPixRequest?): List<KeyPix> {
        val responseItau = clientItau.retrieveAccountClient(request?.idClient)

        if (responseItau.status == HttpStatus.NOT_FOUND)
            throw IllegalStateException("Account not found")

        return pixRepository.findByIdClient(UUID.fromString(request?.idClient))
    }
}
