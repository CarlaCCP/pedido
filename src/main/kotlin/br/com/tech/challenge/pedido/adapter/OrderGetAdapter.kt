package br.com.tech.challenge.pedido.adapter

import br.com.tech.challenge.pedido.core.dto.ProductDTO
import br.com.tech.challenge.pedido.core.entity.order.Order
import br.com.tech.challenge.pedido.core.valueObject.status.Status
import java.time.LocalDateTime

data class OrderGetAdapter(
  val id: String? = null,
  val products: List<ProductDTO>,
  val createdAt: LocalDateTime? = null,
  val preco: Double? = null,
  val status: String? = null
) {
  companion object {
    fun fromOrder(order: Order?) =
      order?.products?.let {
        OrderGetAdapter(
          order.id,
          it.map { product -> ProductDTO.fromProduct(product) },
          order.createdAt,
          order.preco,
          order.orderStatus?.description
        )
      }
  }

  fun toPedido() =
      Order(
          products = products.map {
            it.toProduct()
          },
          preco = products.sumOf { it.preco },
          orderStatus = status?.let { Status.getByDescription(it) },
          createdAt = createdAt,
      )
}