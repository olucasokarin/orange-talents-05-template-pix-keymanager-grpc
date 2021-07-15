package br.com.zupedu.pix.endpoints

import br.com.zupedu.grpc.RemovePixRequest
import br.com.zupedu.grpc.RemovePixServiceGrpc
import br.com.zupedu.pix.externalConnections.itau.ClientItau
import br.com.zupedu.pix.externalConnections.itau.ClientItauAccountResponse
import br.com.zupedu.pix.externalConnections.itau.ClientItauResponse
import br.com.zupedu.pix.model.KeyPix
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
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import java.util.*
import javax.inject.Inject

@MicronautTest(transactional = false)
internal class RemovePixServiceGrpcEndpointTest(
    private val pixRepository: PixRepository,
    private val removeGrpcClient: RemovePixServiceGrpc.RemovePixServiceBlockingStub
) {

    @Inject
    private lateinit var clientItau: ClientItau

    private companion object {
        private val CLIENT_ID = UUID.randomUUID()
    }

    @AfterEach
    internal fun tearDown() {
        pixRepository.deleteAll()
    }

    @Test
    fun`should be remove a pix key`() {
        //scenario
        val savedPixKey = pixRepository.save(createNewKey())

        `when`(clientItau.retrieveAccountClient(idClient = CLIENT_ID.toString()))
            .thenReturn(HttpResponse.ok(dataClientAccountResponse()))

        //actions
        val response = removeGrpcClient.remove(
            RemovePixRequest.newBuilder()
                .setIdClient(CLIENT_ID.toString())
                .setIdPixKey(savedPixKey.externalId.toString())
                .build()
        )

        //assertions
        with(response) {
            assertEquals("Removed", status)
        }
    }

    @Test
    fun`should not be remove a key when a key not existing`() {
        //acenario
        val idPixKey = UUID.randomUUID().toString()

        //actions
        val assertThrows = assertThrows<StatusRuntimeException> {
            removeGrpcClient.remove(
                RemovePixRequest.newBuilder()
                    .setIdClient(CLIENT_ID.toString())
                    .setIdPixKey(idPixKey)
                    .build()
            )
        }

        //assertions
        with(assertThrows) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Key Pix '${idPixKey}' not existing", status.description)
        }
    }

    @Test
    fun`should not be remove a key when an account not found`() {
        //acenario
        val savedPixKey = pixRepository.save(createNewKey())
        val userNotFound = UUID.randomUUID().toString()

        `when`(clientItau.retrieveAccountClient(idClient = userNotFound))
            .thenReturn(HttpResponse.notFound())

        //actions
        val assertThrows = assertThrows<StatusRuntimeException> {
            removeGrpcClient.remove(
                RemovePixRequest.newBuilder()
                    .setIdClient(userNotFound)
                    .setIdPixKey(savedPixKey.externalId.toString())
                    .build()
            )
        }

        //assertions
        with(assertThrows) {
            assertEquals(Status.FAILED_PRECONDITION.code, status.code)
            assertEquals("Account not found", status.description)
        }
    }

    @Test
    fun`should not be remove a key when key not belongs to user`() {
        //acenario
        val savedPixKey = pixRepository.save(createNewKey())
        val savedPixKeyOuterUser = pixRepository.save(KeyPix(
            idClient = UUID.randomUUID(),
            typeKey = TypeKey.EMAIL,
            valueKey = "test@email.com",
            typeAccount = TypeAccount.CHECKING_ACCOUNT
        ))

        `when`(clientItau.retrieveAccountClient(idClient = CLIENT_ID.toString()))
            .thenReturn(HttpResponse.ok(dataClientAccountResponse()))

        //actions
        val assertThrows = assertThrows<StatusRuntimeException> {
            removeGrpcClient.remove(
                RemovePixRequest.newBuilder()
                    .setIdClient(savedPixKey.idClient.toString())
                    .setIdPixKey(savedPixKeyOuterUser.externalId.toString())
                    .build()
            )
        }

        //assertions
        with(assertThrows) {
            assertEquals(Status.FAILED_PRECONDITION.code, status.code)
            assertEquals("This pix key not belongs to this client", status.description)
        }
    }

    @Test
    fun`should to be validate value key is not compatible with a type key`() {
        //actions
        val assertThrows = assertThrows<StatusRuntimeException> {
                removeGrpcClient.remove(RemovePixRequest.newBuilder().build())
        }

        //assertions
        with(assertThrows) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Invalid Data", status.description)
        }
    }

    @MockBean(ClientItau::class)
    fun clientItau() : ClientItau =
        Mockito.mock(ClientItau::class.java)

    private fun dataAccountResponse() =
        ClientItauResponse (
            tipo = "CONTA_CORRENTE",
            numero = "483201"
        )

    private fun createNewKey() =
        KeyPix(
            idClient = CLIENT_ID,
            typeKey = TypeKey.EMAIL,
            valueKey = "already_key@email.com",
            typeAccount = TypeAccount.CHECKING_ACCOUNT
        )


    @Factory
    class Clients {
        @Bean
        fun removeBlockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel)
                : RemovePixServiceGrpc.RemovePixServiceBlockingStub =
            RemovePixServiceGrpc.newBlockingStub(channel)
    }

    private fun dataClientAccountResponse() =
        ClientItauAccountResponse(
            id = CLIENT_ID.toString(),
            nome = "John Doe"
        )
}
