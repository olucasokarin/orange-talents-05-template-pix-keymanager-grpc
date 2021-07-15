package br.com.zupedu.pix.externalConnections.bcb

import br.com.zupedu.pix.externalConnections.bcb.requests.CreatePixKeyRequest
import br.com.zupedu.pix.externalConnections.bcb.responses.CreatePixResponse
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Consumes
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Produces
import io.micronaut.http.client.annotation.Client
import io.micronaut.retry.annotation.Fallback

@Client("\${URL.EXTERNAL.BCB}")
interface ClientBcb {

    @Post("/api/v1/pix/keys")
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    fun registerKey(@Body createPixKeyRequest: CreatePixKeyRequest) : HttpResponse<CreatePixResponse>
}


@Fallback
class ClientBcbFallback : ClientBcb {
    override fun registerKey(createPixKeyRequest: CreatePixKeyRequest): HttpResponse<CreatePixResponse> {
        return HttpResponse.unprocessableEntity()
    }
}
