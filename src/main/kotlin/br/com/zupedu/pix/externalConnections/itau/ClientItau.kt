package br.com.zupedu.pix.externalConnections.itau

import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Consumes
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client

@Client("\${URL.EXTERNAL.ITAU}")
interface ClientItau {

    @Get("/api/v1/clientes/{id}/contas{?tipo}")
    @Consumes(MediaType.APPLICATION_JSON)
    fun retrieve(@PathVariable id: String?, @QueryValue tipo: String?): HttpResponse<ClientItauResponse>

    @Get("/api/v1/clientes/{idClient}")
    fun retrieveAccountClient(@PathVariable idClient: String?) : HttpResponse<ClientItauAccountResponse>
}

data class ClientItauAccountResponse(
    val id: String,
    val nome: String
)

data class ClientItauResponse(
    val tipo: String,
    val instituicao: Instituicao,
    val agencia: String,
    val numero: String,
    val titular: Titular
) {
    data class Titular(
        val id: String,
        val nome: String,
        val cpf: String
    )

    data class Instituicao(
        val nome: String,
        val ispb: String
    )
}
