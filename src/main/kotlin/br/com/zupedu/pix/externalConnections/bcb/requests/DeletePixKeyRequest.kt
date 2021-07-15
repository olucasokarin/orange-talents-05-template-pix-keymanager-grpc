package br.com.zupedu.pix.externalConnections.bcb.requests

data class DeletePixKeyRequest(
    val key: String,
    val participant: String
)
