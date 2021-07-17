package br.com.zupedu.pix.endpoints

import br.com.zupedu.grpc.RetrievePixRequest
import br.com.zupedu.grpc.RetrievePixServiceGrpc
import br.com.zupedu.pix.externalConnections.bcb.ClientBcb
import br.com.zupedu.pix.externalConnections.bcb.requests.*
import br.com.zupedu.pix.model.Institution
import br.com.zupedu.pix.model.KeyPix
import br.com.zupedu.pix.model.Owner
import br.com.zupedu.pix.model.enums.TypeAccount
import br.com.zupedu.pix.model.enums.TypeKey
import br.com.zupedu.pix.repository.PixRepository
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import java.util.*
import javax.inject.Inject

@MicronautTest(transactional = false)
internal class RetrievePixServiceGrpcEndpointTest(
    @Inject private val pixRepository: PixRepository,
    @Inject private val retrieveGrpcClient: RetrievePixServiceGrpc.RetrievePixServiceBlockingStub
) {

    @Inject
    private lateinit var clientBcb: ClientBcb

    companion object {
        private val CLIENT_ID = UUID.randomUUID()
    }

    @BeforeEach
    internal fun setUp() {
        pixRepository.deleteAll()
    }

    //internal access

    @Test
    fun`should be retrieved a pix key from internal access`() {
        //scenario
        val savedPix = pixRepository.save(createNewKey())

        `when`(clientBcb.retrieveKey(key = savedPix.valueKey))
            .thenReturn(HttpResponse.ok())

        //actions
        val retrieveResponse = retrieveGrpcClient.retrieve(retrieveInternalAccessGrpcRequest(savedPix))

        //assertions
        with(retrieveResponse) {
            assertEquals(savedPix.externalId.toString(), idPix)
            assertEquals(savedPix.idClient.toString(), idClient)
            assertEquals(savedPix.valueKey, valueKey)
            assertEquals(savedPix.typeKey.name, typeKey.name)
            assertEquals(savedPix.typeAccount.name, institution.typeAccount.name)
        }
    }

    @Test
    fun`should not be retrieved when not found in bd from internal access`() {
        //actions
        val assertThrows = assertThrows<StatusRuntimeException> {
            retrieveGrpcClient.retrieve(
                RetrievePixRequest.newBuilder()
                    .setIdPix(
                        RetrievePixRequest.MessageInternal.newBuilder()
                            .setIdPix(UUID.randomUUID().toString())
                            .setIdClient(UUID.randomUUID().toString())
                            .build()
                    )
                    .build()
            )
        }

        //assertions
        with(assertThrows) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Pix not found", status.description)
        }
    }

    @Test
    fun`should not be retrieved when not found on system BCB from internal access`() {
        //scenario
        val savedPix = pixRepository.save(createNewKey())

        `when`(clientBcb.retrieveKey(savedPix.valueKey))
            .thenReturn(HttpResponse.notFound())

        //actions
        val assertThrows = assertThrows<StatusRuntimeException> {
            retrieveGrpcClient.retrieve(
                RetrievePixRequest.newBuilder()
                    .setIdPix(
                        RetrievePixRequest.MessageInternal.newBuilder()
                            .setIdPix(savedPix.externalId.toString())
                            .setIdClient(savedPix.idClient.toString())
                            .build()
                    )
                    .build()
            )
        }

        //assertions
        with(assertThrows) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Pix not found on BCB", status.description)
        }
    }

    // External Access

    @Test
    fun`should be retrieved a pix key from external access`() {
        //scenario
        val savedPix = pixRepository.save(createNewKey())

        `when`(clientBcb.retrieveKey(key = savedPix.valueKey))
            .thenReturn(HttpResponse.ok())

        //actions
        val retrieveResponse = retrieveGrpcClient.retrieve(retrieveExternalAccessGrpcRequest(savedPix))

        //assertions
        with(retrieveResponse) {
            assertEquals("", "")
            assertEquals("", "")
            assertEquals(savedPix.valueKey, valueKey)
            assertEquals(savedPix.typeKey.name, typeKey.name)
            assertEquals(savedPix.typeAccount.name, institution.typeAccount.name)
        }
    }

    @Test
    fun`should not be retrieved when not found in bd from external access`() {
        //scenario
        `when`(clientBcb.retrieveKey("any_data"))
            .thenReturn(HttpResponse.ok())

        //actions
        val assertThrows = assertThrows<StatusRuntimeException> {
            retrieveGrpcClient.retrieve(
                RetrievePixRequest.newBuilder()
                    .setValuePix("any_data")
                    .build()
            )
        }

        //assertions
        with(assertThrows) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Pix not found", status.description)
        }
    }

    @Test
    fun`should not be retrieved when not found on system BCB from external access`() {
        //scenario
        val savedPix = pixRepository.save(createNewKey())

        `when`(clientBcb.retrieveKey(savedPix.valueKey))
            .thenReturn(HttpResponse.notFound())

        //actions
        val assertThrows = assertThrows<StatusRuntimeException> {
            retrieveGrpcClient.retrieve(
                RetrievePixRequest.newBuilder()
                    .setValuePix(savedPix.valueKey)
                    .build()
            )
        }

        //assertions
        with(assertThrows) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Pix not found", status.description)
        }
    }

    // outer tests

    @Test
    fun`should not be retrieved when not passed any data`() {
        //actions
        val assertThrows = assertThrows<StatusRuntimeException> {
            retrieveGrpcClient.retrieve(RetrievePixRequest.newBuilder().build())
        }

        //assertions
        with(assertThrows) {
           assertEquals(Status.INVALID_ARGUMENT.code, status.code)
           assertEquals("Invalid data informed", status.description)
        }
    }

    @Test
    fun`should not be retrieved when passed some invalid data`() {
        //actions
        val assertThrows = assertThrows<StatusRuntimeException> {
            retrieveGrpcClient.retrieve(RetrievePixRequest.newBuilder().setValuePix("").build())
        }

        //assertions
        with(assertThrows) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Invalid Data", status.description)
        }
    }

    private fun retrieveInternalAccessGrpcRequest(savedPix: KeyPix): RetrievePixRequest? {

        return RetrievePixRequest.newBuilder()
            .setIdPix(RetrievePixRequest.MessageInternal.newBuilder()
                .setIdPix(savedPix.externalId.toString())
                .setIdClient(savedPix.idClient.toString())
                .build())
            .build()
    }

    private fun retrieveExternalAccessGrpcRequest(savedPix: KeyPix): RetrievePixRequest? {

        return RetrievePixRequest.newBuilder()
            .setValuePix(savedPix.valueKey)
            .build()
    }

    private fun createNewKey() =
        KeyPix(
            idClient = CLIENT_ID,
            typeKey = TypeKey.EMAIL,
            valueKey = "already_key@email.com",
            typeAccount = TypeAccount.CHECKING_ACCOUNT,
            branch = "0001",
            accountNumber = "048967",
            institution = Institution(
                nome = "My Bank",
                ispb = "60701190"
            ),
            owner = Owner(
                nome = "John Doe",
                cpf = "43951423030"
            )
        )

    private fun createPixBcbRequest(): CreatePixKeyRequest {
        val keyPix = createNewKey()

        return CreatePixKeyRequest(
            keyType = KeyTypeBCB.by(keyPix.typeKey),
            key = keyPix.valueKey,
            bankAccount = BankAccountRequest(
                participant = keyPix.institution.ispb,
                branch = keyPix.branch,
                accountNumber = keyPix.accountNumber,
                accountType = AccountType.by(keyPix.typeAccount)
            ),
            owner = OwnerRequest(
                type = OwnerType.NATURAL_PERSON,
                name = keyPix.owner.nome,
                taxIdNumber = keyPix.owner.cpf
            )
        )
    }

    @Factory
    class Clients {
        @Bean
        fun retrieveBlockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel) =
            RetrievePixServiceGrpc.newBlockingStub(channel)
    }

    @MockBean(ClientBcb::class)
    fun clientBcb() =
        Mockito.mock(ClientBcb::class.java)
}
