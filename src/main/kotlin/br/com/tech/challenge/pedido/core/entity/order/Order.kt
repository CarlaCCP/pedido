package br.com.tech.challenge.pedido.core.entity.order


import br.com.tech.challenge.pedido.core.entity.product.Product
import br.com.tech.challenge.pedido.core.valueObject.status.Status
import br.com.tech.challenge.pedido.interfaces.db.IOrder
import java.time.LocalDateTime
import java.util.*

data class Order(
    override var id: String? = UUID.randomUUID().toString(),
    override var products: List<Product>,
    override var createdAt: LocalDateTime? = null,
    override var preco: Double,
    override var orderStatus: Status? = null,
) : IOrder
