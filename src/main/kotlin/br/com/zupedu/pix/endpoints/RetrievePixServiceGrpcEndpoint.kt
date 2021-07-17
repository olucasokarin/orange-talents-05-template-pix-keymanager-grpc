package br.com.zupedu.pix.endpoints

import br.com.zupedu.grpc.*
import br.com.zupedu.pix.endpoints.utils.convertGrpcRequestToInternalRequest
import br.com.zupedu.pix.externalConnections.bcb.ClientBcb
import br.com.zupedu.pix.model.KeyPix
import br.com.zupedu.pix.repository.PixRepository
import br.com.zupedu.shared.handlingErrors.ErrorAroundHandler
import com.google.protobuf.Timestamp
import io.grpc.stub.StreamObserver
import io.micronaut.validation.validator.Validator
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@ErrorAroundHandler
class RetrievePixServiceGrpcEndpoint(
    @Inject private val pixRepository: PixRepository,
    @Inject private val clientBcb: ClientBcb,
    @Inject private val validator: Validator
) : RetrievePixServiceGrpc.RetrievePixServiceImplBase() {

    override fun retrieve(
        request: RetrievePixRequest?,
        responseObserver: StreamObserver<RetrievePixReply>?
    ) {
        val retrievePixService = request?.convertGrpcRequestToInternalRequest(validator)
        val keyPix = retrievePixService?.filter(pixRepository, clientBcb)

        responseObserver?.onNext(convertToRetrieveReply(keyPix!!, request.decideCase))
        responseObserver?.onCompleted()
    }
}

fun convertToRetrieveReply(keyPix: KeyPix, decideCase: RetrievePixRequest.DecideCase): RetrievePixReply =
    RetrievePixReply.newBuilder()
        .setIdPix(when(decideCase) {
            RetrievePixRequest.DecideCase.IDPIX -> keyPix.externalId.toString()
            else -> ""
        })
        .setIdClient(when(decideCase) {
            RetrievePixRequest.DecideCase.IDPIX -> keyPix.idClient.toString()
            else -> ""
        })
        .setTypeKey(TypeKey.valueOf(keyPix.typeKey.name))
        .setValueKey(keyPix.valueKey)
        .setOwnerName(keyPix.owner.nome)
        .setOwnerCPF(keyPix.owner.cpf)
        .setInstitution(RetrievePixReply.Institution.newBuilder()
            .setName(keyPix.institution.nome)
            .setBranch(keyPix.branch)
            .setNumberAccount(keyPix.accountNumber)
            .setTypeAccount(TypeAccount.valueOf(keyPix.typeAccount.name))
            .build())
        .setCreatedAt(keyPix.createAt.run {
            val formattedDate = atZone(ZoneId.of("UTC")).toInstant()
            Timestamp.newBuilder()
                .setSeconds(formattedDate.epochSecond)
                .setNanos(formattedDate.nano)
                .build()
        })
        .build()
