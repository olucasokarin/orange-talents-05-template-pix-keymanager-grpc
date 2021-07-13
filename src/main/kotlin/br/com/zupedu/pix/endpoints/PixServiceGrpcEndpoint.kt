package br.com.zupedu

import br.com.zupedu.grpc.PixServiceGrpc
import br.com.zupedu.grpc.RegisterPixReply
import br.com.zupedu.grpc.RegisterPixRequest
import br.com.zupedu.pix.RegisterPixService
import br.com.zupedu.pix.ValidKeyPix
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

        val pixKey = service.register(requestPixGrpc!!)

        val response = RegisterPixReply.newBuilder()
            .setIdClient(pixKey.idClient.toString())
            .setIdPix(pixKey.externalId.toString())
            .build()

        responseObserver?.onNext(response)
        responseObserver?.onCompleted()
    }
}

fun RegisterPixRequest.convertRequestGrpcToRequestKey() =
    KeyPixRequest(
        idClient = idClient,
        typeKey = when(typeKey) {
            br.com.zupedu.grpc.TypeKey.UNKNOWN_KEY -> null
            else -> TypeKey.valueOf(typeKey.name)
        },
        valueKey = valueKey,
        typeAccount = when (typeAccount) {
            br.com.zupedu.grpc.TypeAccount.UNKNOWN_ACCOUNT -> null
            else -> TypeAccount.valueOf(typeAccount.name)
        }
    )

@ValidKeyPix
@Introspected
data class KeyPixRequest(
    @field:NotBlank
    val idClient: String?,
    @field:NotNull
    val typeKey: TypeKey?,
    @field:Size(max= 77)
    val valueKey: String?,
    @field:NotNull
    val typeAccount: TypeAccount?
) {
    fun convertToEntityPix() =
        KeyPix(
            idClient = UUID.fromString(this.idClient),
            typeAccount = TypeAccount.valueOf(this.typeAccount!!.name),
            typeKey = TypeKey.valueOf(this.typeKey!!.name),
            valueKey = if (this.typeKey == TypeKey.RANDOM_KEY) UUID.randomUUID().toString() else this.valueKey!!
        )
}
