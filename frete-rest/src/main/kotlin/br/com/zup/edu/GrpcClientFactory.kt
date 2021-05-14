package br.com.zup.edu

import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import javax.inject.Singleton

/**
 *responsável por adicionar o stub no contexto do micronaut através do conceito de Factory
 */
@Factory
class GrpcClientFactory {

    @Singleton
    fun fretesClientStub(@GrpcChannel("fretes") channel: ManagedChannel): FreteServiceGrpc.FreteServiceBlockingStub? {
        return FreteServiceGrpc.newBlockingStub(channel)
    }
}