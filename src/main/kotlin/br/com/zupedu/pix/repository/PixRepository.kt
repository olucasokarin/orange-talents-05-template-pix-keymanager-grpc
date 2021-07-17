package br.com.zupedu.pix.repository

import br.com.zupedu.pix.model.KeyPix
import io.micronaut.data.annotation.Query
import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import java.util.*

@Repository
interface PixRepository : JpaRepository<KeyPix, Long> {

    fun existsByValueKey(id: String?): Boolean

    fun findByExternalId(idPixKey: UUID?) : Optional<KeyPix>

    @Query("select p from KeyPix p where p.idClient = :idClient and p.externalId = :idPix")
    fun findByIdClient(idClient: UUID?, idPix: UUID?): Optional<KeyPix>

    fun findByValueKey(value: String): Optional<KeyPix>

    fun findByIdClient(idClient: UUID?) : List<KeyPix>
}
