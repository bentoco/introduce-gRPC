package br.com.zup.edu

import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class CarrosEndpointTest(
    val grpcClient: CarrosGrpcServiceGrpc.CarrosGrpcServiceBlockingStub,
    val repository: CarroRepository
) {

    /**
     * 1 - happy path (ok)
     * 2 - quando ja existe carro com placa
     * 3 - quando os dados de entrada são invalidos
     */



    @Test
    internal fun `deve adicionar um novo carro`() {
        // cenário (optional)
        repository.deleteAll()

        // ação
        val response = grpcClient.adicionar(
            CarroRequest.newBuilder()
                .setModelo("palio")
                .setPlaca("HPX-1234")
                .build()
        )

        // validação
        with(response) {
            assertNotNull(id)
            assertTrue(repository.existsById(id)) // efeito colateral
        }
    }

    @BeforeEach
    internal fun setUp() {
        repository.deleteAll()
    }

    @Test
    fun `nao deve adicionar novo carro quando carro com placa ja existe`() {
        // cenário
        repository.save(Carro("palio", "HPX-1234"))

        // ação
        val exception = assertThrows<StatusRuntimeException>("should throw an exception") {
            grpcClient.adicionar(
                CarroRequest.newBuilder()
                    .setModelo("palio")
                    .setPlaca("HPX-1234")
                    .build()
            )
        }

        // validação
        with(exception) {
            assertEquals(Status.ALREADY_EXISTS, status.code.toStatus())
            assertEquals("carro com placa existente", status.description)
        }
    }

    @Test
    fun `nao deve adcionar novo carro quando dados de entrada forem invalidos`() {
        val exception = assertThrows<StatusRuntimeException>("should throw an exception") {
            grpcClient.adicionar(
                CarroRequest
                    .newBuilder()
                    .build()
            )
        }
        with(exception) {
            assertEquals(Status.INVALID_ARGUMENT, status.code.toStatus())
            assertEquals("dados de entrada inválidos", status.description)
            // TODO: verificar as violações da bean validation

        }
    }

    @Factory
    class Clients {
        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): CarrosGrpcServiceGrpc.CarrosGrpcServiceBlockingStub? {
            return CarrosGrpcServiceGrpc.newBlockingStub(channel)
        }
    }
}