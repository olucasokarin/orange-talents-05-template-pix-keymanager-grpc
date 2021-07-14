package br.com.zupedu.pix.model

import br.com.zupedu.pix.model.enums.TypeAccount
import br.com.zupedu.pix.model.enums.TypeKey
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Entity
class KeyPix(
    @field:NotNull
    val idClient: UUID,

    @field:NotNull
    @Enumerated(EnumType.STRING)
    val typeKey: TypeKey,

    @field:NotNull
    @Enumerated(EnumType.STRING)
    val typeAccount: TypeAccount,

    @field:NotBlank
    @Column(unique = true, nullable = false)
    var valueKey: String
) {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    var externalId: UUID? = UUID.randomUUID();

    val createAt: LocalDateTime = LocalDateTime.now()

    fun pixBelongsToTheClient(value: String?) =
        this.idClient.toString() == value

}
