package br.com.tech.challenge.pedido.interfaces.gateway

import br.com.tech.challenge.pedido.core.entity.order.Order
import br.com.tech.challenge.pedido.core.valueObject.status.Status
import com.amazonaws.services.dynamodbv2.document.DeleteItemOutcome
import org.springframework.stereotype.Repository

@Repository
interface IOrderGateway {

  fun findAll(): List<Order>?

  fun findFirst(): Order?

  fun findByStatus(status: Status): List<Order>?

  fun findById(id: String): Order?

  fun save(order: Order): Order

  fun deleteById(id: String): DeleteItemOutcome?


}