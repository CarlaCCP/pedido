package br.com.tech.challenge.pedido.adapter

import br.com.tech.challenge.pedido.core.entity.order.Order


data class OrderAdapter(
    val id: String? = null,
    val productsDescriptions: List<String>?,
    val preco: Double? = null,
    val status: String?,
    val tempoEspera: String? = null,
    val acompanhamentoURL: String? = null,
) {
  companion object {
    fun fromOrderToSummary(order: Order) =
      OrderAdapter(
        id = order.id!!,
        productsDescriptions = order.products.map { it.descricao },
        preco = order.preco,
        status = order.orderStatus!!.description,
        tempoEspera = "30 minutos",
        acompanhamentoURL = "/pedido/${order.id}"
      )
  }
}
