package br.com.zupedu.pix.endpoints

import br.com.zupedu.grpc.PixServiceGrpc
import br.com.zupedu.grpc.RegisterPixRequest
import br.com.zupedu.grpc.TypeAccount
import br.com.zupedu.grpc.TypeKey
import br.com.zupedu.pix.externalConnections.itau.ClientItau
import br.com.zupedu.pix.externalConnections.itau.ClientItauResponse
import br.com.zupedu.pix.model.KeyPix
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
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import java.util.*
import javax.inject.Inject

@MicronautTest(transactional = false)
internal class PixServiceGrpcEndpointTest(
    private val pixRepository: PixRepository,
    private val grpcClient: PixServiceGrpc.PixServiceBlockingStub
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
    fun `should be register a new pix key`() {
        //scenario
        val NewKey = createNewKey()
        `when`(clientItau.retrieve(id = CLIENT_ID.toString(), tipo = "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.ok(dataAccountResponse()))

        //action
        val responseGrpc = grpcClient.register(
            RegisterPixRequest.newBuilder()
                .setIdClient(NewKey.idClient.toString())
                .setTypeKey(TypeKey.valueOf(NewKey.typeKey.name))
                .setValueKey(NewKey.valueKey)
                .setTypeAccount(TypeAccount.valueOf(NewKey.typeAccount.name))
                .build()
        )

        val confirmKey = pixRepository.existsByValueKey(NewKey.valueKey)

        //assertions
        with(responseGrpc) {
            assertEquals(CLIENT_ID.toString(), idClient)
            assertNotNull(idPix)
            assertTrue(confirmKey)
        }
    }

    @Test
    fun `should not be register a new pix key when a same key already exists`() {
        //scenario
        pixRepository.save(createNewKey())

        //action
        val assertThrows = assertThrows<StatusRuntimeException> {
            grpcClient.register(
                RegisterPixRequest.newBuilder()
                    .setIdClient(CLIENT_ID.toString())
                    .setTypeKey(TypeKey.EMAIL)
                    .setValueKey("already_key@email.com")
                    .setTypeAccount(TypeAccount.CHECKING_ACCOUNT)
                    .build()
            )
        }

        //validation
        with(assertThrows) {
            assertEquals(Status.ALREADY_EXISTS.code, status.code)
            assertEquals("Key Pix 'already_key@email.com' existing", status.description)
        }
    }

    @Test
    fun `should not be register a new pix key when client not found`() {
        //scenario
        `when`(clientItau.retrieve(id= CLIENT_ID.toString(), tipo = "CONTA_POUPANCA"))
            .thenReturn(HttpResponse.notFound())

        //action
        val assertThrows = assertThrows<StatusRuntimeException> {
            grpcClient.register(
                RegisterPixRequest.newBuilder()
                    .setIdClient(CLIENT_ID.toString())
                    .setTypeKey(TypeKey.EMAIL)
                    .setValueKey("test@email.com")
                    .setTypeAccount(TypeAccount.SAVINGS_ACCOUNT)
                    .build()
            )
        }

        //assertion
        with(assertThrows) {
            assertEquals(Status.FAILED_PRECONDITION.code, status.code)
            assertEquals("Client or account not found", status.description)
        }
    }

    @Test
    fun `should not be register when some data is invalid`() {
        //scenario
        `when`(clientItau.retrieve(id = CLIENT_ID.toString(), tipo = "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.ok(dataAccountResponse()))

        //actions
        val assertThrows = assertThrows<StatusRuntimeException> {
            grpcClient.register(
                RegisterPixRequest.newBuilder()
                    .setIdClient(CLIENT_ID.toString())
                    .setTypeKey(TypeKey.UNKNOWN_KEY)
                    .setValueKey("test@email.com")
                    .setTypeAccount(TypeAccount.UNKNOWN_ACCOUNT)
                    .build()
            )
        }

        //assertions
        with(assertThrows) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Invalid Data", status.description)
        }
    }

    @Test
    fun`should to be validate value key is not compatible with a type key`() {
        //scenarios
        `when`(clientItau.retrieve(id = CLIENT_ID.toString(), tipo = "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.ok(dataAccountResponse()))

        //actions
        val assertThrows = assertThrows<StatusRuntimeException> {
            grpcClient.register(
                RegisterPixRequest.newBuilder()
                    .setIdClient(CLIENT_ID.toString())
                    .setTypeKey(TypeKey.RANDOM_KEY)
                    .setValueKey("test@email.com")
                    .setTypeAccount(TypeAccount.CHECKING_ACCOUNT)
                    .build()
            )
        }

        //assertions
        with(assertThrows) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Invalid Data", status.description)
        }
    }

    @Factory
    class Clients {
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel)
            : PixServiceGrpc.PixServiceBlockingStub {
            return PixServiceGrpc.newBlockingStub(channel)
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
            typeKey = br.com.zupedu.pix.model.enums.TypeKey.EMAIL,
            valueKey = "already_key@email.com",
            typeAccount = br.com.zupedu.pix.model.enums.TypeAccount.CHECKING_ACCOUNT
        )
}
