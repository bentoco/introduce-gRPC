package br.com.zup.edu

import java.io.FileInputStream
import java.io.FileOutputStream

fun main() {
    val request = FuncionarioRequest.newBuilder()
        .setNome("Yuri Matheus")
        .setCpf("000.000.000-00")
        .setSalario(2000.20)
        .setAtivo(true)
        .setCargo(Cargo.DEV)
        .addEnderecos(
            FuncionarioRequest.Endereco.newBuilder()
                .setLogradouro("Rua das Tabajaras")
                .setCep("00000-00")
                .setComplemento("casa 20")
        )
        .build()
    println(request)

    //gerando binário - escrevemos o objeto (mensagem serializada)
    request.writeTo(FileOutputStream("funcionario-request.bin"))

    //lendo binário - lemos o objeto (mensagem desserializada)
    val request2 = FuncionarioRequest.newBuilder()
        .mergeFrom(FileInputStream("funcionario-request.bin"))
    request2.cargo = Cargo.GERENTE
    println(request2)
}