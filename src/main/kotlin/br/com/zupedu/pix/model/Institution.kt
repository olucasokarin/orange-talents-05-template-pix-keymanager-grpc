package br.com.zupedu.pix.model

import javax.persistence.*

@Embeddable
class Institution(
    @field:Column(name = "institution_nome")
    val nome: String,
    @field:Column(name = "institution_ispb")
    val ispb: String
)
