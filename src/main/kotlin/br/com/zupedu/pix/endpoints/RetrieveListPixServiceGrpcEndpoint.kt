package br.com.zupedu.pix.endpoints

import br.com.zupedu.grpc.*
import br.com.zupedu.pix.RetrieveListService
import br.com.zupedu.pix.model.KeyPix
import br.com.zupedu.shared.handlingErrors.ErrorAroundHandler
import io.grpc.stub.StreamObserver
import io.micronaut.core.annotation.Introspected
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.constraints.NotBlank

@Singleton
@ErrorAroundHandler
class RetrieveListPixServiceGrpcEndpoint(
    @Inject private val service: RetrieveListService
) : RetrieveAllPixServiceGrpc.RetrieveAllPixServiceImplBase() {
    override fun retrieveAll(
        request: RetrieveListRequest?,
        responseObserver: StreamObserver<RetrieveListReply>?
    ) {
        val requestKey = request?.convertToRequestKey()

        val retrieveAllPix = service.retrieveAllPix(requestKey)
        val response = convertEntityToReplyGrpc(retrieveAllPix, requestKey?.idClient)

        responseObserver?.onNext(response)
        responseObserver?.onCompleted()
    }
}

fun RetrieveListRequest.convertToRequestKey(): RetrieveAllKeyPixRequest {
    return RetrieveAllKeyPixRequest(idClient)
}

@Introspected
data class RetrieveAllKeyPixRequest(
    @field:NotBlank
    val idClient: String
)

fun convertEntityToReplyGrpc(pixKeys: List<KeyPix>, idClient: String?): RetrieveListReply? {

    val listPixReply = pixKeys.map { key ->
        RetrieveListReply.ListPix.newBuilder()
            .setIdPix(key.externalId.toString())
            .setTypeKey(TypeKey.valueOf(key.typeKey.name))
            .setValueKey(key.valueKey)
            .setTypeAccount(TypeAccount.valueOf(key.typeAccount.name))
            .setCreatedAt(key.createAt.run {
                val formattedDate = atZone(ZoneId.of("UTC")).toInstant()
                com.google.protobuf.Timestamp.newBuilder()
                    .setSeconds(formattedDate.epochSecond)
                    .setNanos(formattedDate.nano)
                    .build()
            })
            .build()
    }

    return RetrieveListReply.newBuilder()
        .setIdClient(idClient)
        .addAllListPix(listPixReply)
        .build()
}
