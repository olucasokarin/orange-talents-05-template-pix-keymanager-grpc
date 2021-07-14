package br.com.zupedu.pix.repository

import br.com.zupedu.pix.model.KeyPix
import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import java.util.*

@Repository
interface PixRepository : JpaRepository<KeyPix, Long> {

    fun existsByValueKey(id: String?): Boolean

    fun findByExternalId(idPixKey: UUID?) : Optional<KeyPix>
}
