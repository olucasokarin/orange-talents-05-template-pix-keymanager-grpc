package br.com.zupedu

import br.com.zupedu.grpc.*
import br.com.zupedu.pix.RemovePixService
import br.com.zupedu.shared.handlingErrors.ErrorAroundHandler
import io.grpc.stub.StreamObserver
import io.micronaut.core.annotation.Introspected
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.constraints.NotBlank

@Singleton
@ErrorAroundHandler
class RemovePixServiceGrpcEndpoint(
    @Inject private val service: RemovePixService
) : RemovePixServiceGrpc.RemovePixServiceImplBase()  {

     override fun remove(
        request: RemovePixRequest?,
        responseObserver: StreamObserver<RemovePixReply>?
    ) {
        val requestPixGrpc = request?.convertRequestGrpcToRequestKey()
        service.remove(requestPixGrpc!!)

         val response = RemovePixReply.newBuilder()
             .setStatus("Removed")
             .build()

         responseObserver?.onNext(response)
         responseObserver?.onCompleted()
    }
}

fun RemovePixRequest.convertRequestGrpcToRequestKey() =
    PixKeyRemoveRequest(
        idClient = idClient,
        idPixKey = idPixKey
    )

@Introspected
data class PixKeyRemoveRequest(
    @field:NotBlank
    val idClient: String?,
    @field:NotBlank
    val idPixKey: String?,
)
