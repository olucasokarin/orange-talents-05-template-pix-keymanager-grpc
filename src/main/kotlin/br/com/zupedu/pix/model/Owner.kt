package br.com.zupedu.pix.model

import javax.persistence.*

@Embeddable
class Owner(
    @field:Column(name = "owner_nome")
    val nome: String,
    @field:Column(name = "owner_cpf")
    val cpf: String
)
