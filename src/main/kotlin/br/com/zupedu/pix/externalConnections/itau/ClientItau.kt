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
}

data class ClientItauResponse(
    val tipo: String,
    val numero: String
)