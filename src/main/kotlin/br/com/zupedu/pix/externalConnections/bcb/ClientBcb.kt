package br.com.zupedu.pix.externalConnections.bcb

import br.com.zupedu.pix.externalConnections.bcb.requests.CreatePixKeyRequest
import br.com.zupedu.pix.externalConnections.bcb.requests.DeletePixKeyRequest
import br.com.zupedu.pix.externalConnections.bcb.responses.CreatePixResponse
import br.com.zupedu.pix.externalConnections.bcb.responses.DeletePixKeyResponse
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.http.client.annotation.Client
import io.micronaut.retry.annotation.Fallback

@Client("\${URL.EXTERNAL.BCB}")
interface ClientBcb {

    @Post("/api/v1/pix/keys")
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    fun registerKey(@Body createPixKeyRequest: CreatePixKeyRequest) : HttpResponse<CreatePixResponse>

    @Delete("/api/v1/pix/keys/{key}")
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    fun removeKey(@PathVariable key: String, @Body deletePixKeyRequest: DeletePixKeyRequest) : HttpResponse<DeletePixKeyResponse>

    @Get("/api/v1/pix/keys/{key}")
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    fun retrieveKey(@PathVariable key: String) : HttpResponse<Any>
}


@Fallback
class ClientBcbFallback : ClientBcb {
    override fun registerKey(createPixKeyRequest: CreatePixKeyRequest): HttpResponse<CreatePixResponse> {
        return HttpResponse.unprocessableEntity()
    }

    override fun removeKey(key: String, deletePixKeyRequest: DeletePixKeyRequest): HttpResponse<DeletePixKeyResponse> {
        return HttpResponse.status(HttpStatus.FORBIDDEN)
    }

    override fun retrieveKey(key: String): HttpResponse<Any> =
        HttpResponse.badRequest()

}
