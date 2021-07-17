package br.com.zupedu.pix.endpoints

import br.com.zupedu.grpc.RetrieveAllPixServiceGrpc
import br.com.zupedu.grpc.RetrieveListRequest
import br.com.zupedu.pix.externalConnections.itau.ClientItau
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
internal class RetrieveListPixServiceGrpcEndpointTest(
    @Inject private val pixRepository: PixRepository,
    @Inject private val retrieveAllGrpcClient: RetrieveAllPixServiceGrpc.RetrieveAllPixServiceBlockingStub
){

    @Inject
    private lateinit var clientItau: ClientItau

    companion object {
        private val CLIENT_ID = UUID.randomUUID()
    }

    @BeforeEach
    internal fun setUp() {
        pixRepository.deleteAll()
    }

    @Test
    fun`should be retrieved all keys`() {
        //scenario
        val savedKey = pixRepository.save(createNewKey())

        `when`(clientItau.retrieveAccountClient(CLIENT_ID.toString()))
            .thenReturn(HttpResponse.ok())

        //actions
        val retrieveAll = retrieveAllGrpcClient.retrieveAll(RetrieveListRequest.newBuilder().setIdClient(CLIENT_ID.toString()).build())

        //assertions
        with(retrieveAll) {
            assertEquals(CLIENT_ID.toString(), idClient)
            assertTrue(this.listPixList.size > 0)
            assertEquals(savedKey.valueKey, listPixList[0].valueKey)
            assertEquals(savedKey.externalId.toString(), listPixList[0].idPix)
        }
    }

    @Test
    fun`should not be retrieved when not found on system Itau`() {
        //scenario
        `when`(clientItau.retrieveAccountClient(CLIENT_ID.toString()))
            .thenReturn(HttpResponse.notFound())

        //actions
        val assertThrows = assertThrows<StatusRuntimeException> {
            retrieveAllGrpcClient.retrieveAll(
                RetrieveListRequest.newBuilder().setIdClient(CLIENT_ID.toString()).build()
            )
        }

        //assertions
        with(assertThrows) {
            assertEquals(Status.FAILED_PRECONDITION.code, status.code)
            assertEquals("Account not found", status.description)
        }
    }

    @Test
    fun`should be retrieved empty list`() {
        //scenario
        `when`(clientItau.retrieveAccountClient(CLIENT_ID.toString()))
            .thenReturn(HttpResponse.ok())

        //actions
        val retrieveAll = retrieveAllGrpcClient.retrieveAll(
            RetrieveListRequest.newBuilder().setIdClient(CLIENT_ID.toString()).build()
        )

        //assertions
        with(retrieveAll) {
            assertEquals(CLIENT_ID.toString(), idClient)
            assertTrue(this.listPixList.size == 0)
        }
    }

    @Test
    fun `should be validate invalid data`() {
        //actions
        val assertThrows = assertThrows<StatusRuntimeException> {
            retrieveAllGrpcClient.retrieveAll(RetrieveListRequest.newBuilder().build())
        }

        //assertions
        with(assertThrows) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Invalid Data", status.description)
        }
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


    @Factory
    class Clients {
            @Bean
        fun retrieveAllBlockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel) =
            RetrieveAllPixServiceGrpc.newBlockingStub(channel)
    }

    @MockBean(ClientItau::class)
    fun clientItau() =
        Mockito.mock(ClientItau::class.java)
}