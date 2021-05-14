package br.com.zup.edu

import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.grpc.protobuf.StatusProto
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.exceptions.HttpStatusException
import com.google.protobuf.Any
import javax.inject.Inject

@Controller
class CalculadoraFretesController(@Inject val grpcClient: FreteServiceGrpc.FreteServiceBlockingStub) {

    @Get("/api/fretes")
    fun calcula(cep: String): FreteResponse {
        val request: CalculaFreteRequest = CalculaFreteRequest
            .newBuilder()
            .setCep(cep)
            .build()

        try {
            val response = grpcClient
                .calculaFrete(request)

            return FreteResponse(cep = response.cep, valor = response.valor)
        } catch (e: StatusRuntimeException) {

            val description = e.status.description
            val statusCode = e.status.code

            if (statusCode == Status.Code.INVALID_ARGUMENT) {
                throw HttpStatusException(HttpStatus.BAD_REQUEST, description)
            }

            if (statusCode == Status.Code.PERMISSION_DENIED) {
                val statusProto =
                    StatusProto.fromThrowable(e) ?: throw HttpStatusException(HttpStatus.FORBIDDEN, description)

                val anyDatails: Any = statusProto.detailsList[0]
                val errorDetails = anyDatails.unpack(ErroDetails::class.java)

                throw  HttpStatusException(HttpStatus.UNAUTHORIZED, "${errorDetails.code}: ${errorDetails.message}")
            }

            //caso contrario trata como erro inesperado
            throw HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.message)
        }
    }
}

data class FreteResponse(val cep: String, val valor: Double)