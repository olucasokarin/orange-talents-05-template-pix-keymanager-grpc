package br.com.zupedu

import br.com.zupedu.grpc.PixServiceGrpc
import br.com.zupedu.grpc.RegisterPixReply
import br.com.zupedu.grpc.RegisterPixRequest
import br.com.zupedu.pix.RegisterPixService
import br.com.zupedu.pix.model.KeyPix
import br.com.zupedu.pix.model.enums.TypeAccount
import br.com.zupedu.pix.model.enums.TypeKey
import br.com.zupedu.shared.handlingErrors.ErrorAroundHandler
import io.grpc.stub.StreamObserver
import io.micronaut.core.annotation.Introspected
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Singleton
@ErrorAroundHandler
class PixServiceGrpcEndpoint(
    @Inject private val service: RegisterPixService
) : PixServiceGrpc.PixServiceImplBase() {

    override fun register(
        request: RegisterPixRequest?,
        responseObserver: StreamObserver<RegisterPixReply>?
    ) {
        val requestPixGrpc = request?.convertRequestGrpcToRequestKey()

        service.register(requestPixGrpc!!)

        val id = RegisterPixReply.newBuilder().setIdClient(UUID.randomUUID().toString()).build()

        responseObserver?.onNext(id)
        responseObserver?.onCompleted()
    }
}

fun RegisterPixRequest.convertRequestGrpcToRequestKey() =
    KeyPixRequest(
        idClient = idClient,
        typeKey = when(typeKey) {
            br.com.zupedu.grpc.TypeKey.UNKNOWN_KEY -> null
            else -> typeKey.name
        },
        valueKey = valueKey,
        typeAccount = when (typeAccount) {
            br.com.zupedu.grpc.TypeAccount.UNKNOWN_ACCOUNT -> null
            else -> typeAccount.name
        }
    )

@Introspected
data class KeyPixRequest(
    @field:NotBlank
    val idClient: String?,
    @field:NotNull
    val typeKey: String?,
    @field:Size(max= 77)
    val valueKey: String?,
    @field:NotBlank
    val typeAccount: String?
) {
    fun convertToEntityPix() =
        KeyPix(
            idClient = UUID.fromString(this.idClient),
            typeAccount = TypeAccount.valueOf(this.typeAccount!!),
            typeKey = TypeKey.valueOf(this.typeKey!!),
            valueKey = if(TypeKey.valueOf(this.typeKey) == TypeKey.RANDOM_KEY) UUID.randomUUID().toString() else this.valueKey!!
        )
}
