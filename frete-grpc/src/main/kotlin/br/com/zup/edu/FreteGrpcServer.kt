package br.com.zup.edu

import com.google.protobuf.Any
import com.google.rpc.Code
import io.grpc.Status
import io.grpc.protobuf.StatusProto
import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class FreteGrpcServer : FreteServiceGrpc.FreteServiceImplBase() {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun calculaFrete(request: CalculaFreteRequest?, responseObserver: StreamObserver<CalculaFreteReply>?) {
        logger.info("Calculando frete para $request")

        val cep = request?.cep
        if (cep == null || cep.isBlank()) {
            val exception =
                Status.INVALID_ARGUMENT
                    .withDescription("cep deve ser infromado")
                    .asRuntimeException()
            responseObserver?.onError(exception)
        }

        if (!cep!!.matches("[0-9]{5}-[0-9]{3}".toRegex())) {
            val exception =
                Status.INVALID_ARGUMENT
                    .withDescription("cep inválido")
                    .augmentDescription("formato esperado deve ser 99999-999")
                    .asRuntimeException()
            responseObserver?.onError(exception)
        }

        // simular uma verificação de segurança
        if(cep.endsWith("333")){
            val statusProto = com.google.rpc.Status.newBuilder()
                .setCode(Code.PERMISSION_DENIED.number)
                .setMessage("usuário nao pode acessar esse recurso")
                .addDetails(Any.pack(ErroDetails.newBuilder()
                    .setCode(401)
                    .setMessage("token expirado")
                    .build()))
                .build()

            val e = StatusProto.toStatusRuntimeException(statusProto)
            responseObserver?.onError(e)
        }

        var valor = 0.0
        try {
            valor = Random.nextDouble(from = 0.0, until = 140.0)
            if(valor > 100.0){
                throw IllegalArgumentException("Erro inesperado ao executar logica de negocio!")
            }
        } catch (e: Exception) {
            responseObserver?.onError(Status.INTERNAL
                .withDescription(e.message)
                .withCause(e) /// anexado ao status, mas nao enviado ao client
                .asRuntimeException())
        }

        val response = CalculaFreteReply.newBuilder()
            .setCep(cep)
            .setValor(valor)
            .build()

        logger.info("Frete calculado: $response")

        responseObserver!!.onNext(response)
        responseObserver.onCompleted()
    }

}