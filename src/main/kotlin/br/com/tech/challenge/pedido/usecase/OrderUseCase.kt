package br.com.tech.challenge.pedido.usecase


import br.com.tech.challenge.pedido.adapter.OrderGetAdapter
import br.com.tech.challenge.pedido.core.dto.OrderDTO
import br.com.tech.challenge.pedido.core.dto.PaymentDTO
import br.com.tech.challenge.pedido.core.entity.order.Order
import br.com.tech.challenge.pedido.core.valueObject.payment.PaymentEvent
import br.com.tech.challenge.pedido.core.valueObject.status.Status
import br.com.tech.challenge.pedido.interfaces.client.IPaymentWebhookClient
import br.com.tech.challenge.pedido.interfaces.gateway.IOrderGateway
import mu.KotlinLogging
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException
import java.time.LocalDateTime

class OrderUseCase {

  private val log = KotlinLogging.logger {}
  fun getOrder(orderGateway: IOrderGateway, id: String): Order? = orderGateway.findById(id)

  fun getOrderByStatus(orderGateway: IOrderGateway, status: String): List<Order>? =
    Status.getByDescription(status)?.let {
      orderGateway.findByStatus(it)
    }

  fun createOrder(
    orderGateway: IOrderGateway,
    orderRequest: OrderDTO,
    paymentClient: IPaymentWebhookClient
  ): Order {
//    orderRequest.products.map {
//      if (productRepository.findById(it.id!!) == null)
//        throw NotFoundException()
//    }
    val order = orderGateway.save(
      OrderGetAdapter(products = orderRequest.products)
        .toPedido()
        .copy(
          createdAt = LocalDateTime.now(),
          orderStatus = Status.RECEBIDO
        )
    )
    val paymentResponse = paymentClient.createPayment(order.id!!)
    log.info { "Pagamento com status ${paymentResponse.name}" }
    return if (paymentResponse != PaymentEvent.CREATED) {
      orderGateway.save(
        OrderGetAdapter(products = orderRequest.products)
          .toPedido()
          .copy(
            createdAt = LocalDateTime.now(),
            orderStatus = Status.NEGADO
          )
      )
    } else {
      order
    }
  }

  fun deleteOrder(orderGateway: IOrderGateway, id: String) = orderGateway.deleteById(id)

  fun getOrders(orderGateway: IOrderGateway) = orderGateway.findAll()

  fun deleteOrderByStatus(orderGateway: IOrderGateway) {
    val listOrders: List<Order>? = orderGateway.findByStatus(Status.FINALIZADO)
    println(listOrders)
    if (!listOrders.isNullOrEmpty()) {
      listOrders.map {
        orderGateway.deleteById(it.id!!)
        log.info { "Pedido ${it.id} finalizado removido da fila" }
      }
    }
  }

  fun updateOrderByStatus(orderGateway: IOrderGateway, status: Status) {
    val order: List<Order>? = orderGateway.findByStatus(status)

    order?.map {
      orderGateway.save(
        it.copy(
          orderStatus = if (updateStatus(it.orderStatus) == null) it.orderStatus else updateStatus(it.orderStatus),
          id = it.id
        )
      )
      log.info { "Pedido ${it.id} foi atualizado para o status ${updateStatus(it.orderStatus)}" }
    }
  }

  fun updatePaymentOrder(orderGateway: IOrderGateway, paymentDTO: PaymentDTO): PaymentDTO {
    val order = orderGateway.findById(paymentDTO.id)
    if (order != null && paymentDTO.event == PaymentEvent.APPROVED) {
      orderGateway.save(
        order.copy(
          orderStatus = Status.EM_PREPARACAO
        )
      )
      log.info { "Pagamento de pedido ${order.id} realizado com sucesso" }
    } else if (order != null && paymentDTO.event == PaymentEvent.DENIED) {
      orderGateway.save(
        order.copy(
          orderStatus = Status.NEGADO
        )
      )
      log.info { "Pagamento de pedido ${order.id} negado" }
    } else {
      if (order != null) {
        orderGateway.save(order.copy(orderStatus = Status.NEGADO))
        log.info { "Pagamento de pedido ${order.id} negado" }
      }
    }
    return paymentDTO
  }

  private fun updateStatus(status: Status?): Status? =
    when (status) {
//      Status.RECEBIDO -> Status.EM_PREPARACAO
      Status.EM_PREPARACAO -> Status.PRONTO
      Status.PRONTO -> Status.FINALIZADO
      else -> null
    }
}