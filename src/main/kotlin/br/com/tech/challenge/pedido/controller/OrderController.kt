package br.com.tech.challenge.pedido.controller

import br.com.tech.challenge.pedido.adapter.OrderAdapter
import br.com.tech.challenge.pedido.adapter.OrderGetAdapter
import br.com.tech.challenge.pedido.config.OrderConfig
import br.com.tech.challenge.pedido.core.dto.OrderDTO
import br.com.tech.challenge.pedido.core.dto.PaymentDTO
import br.com.tech.challenge.pedido.interfaces.client.IPaymentWebhookClient
import br.com.tech.challenge.pedido.interfaces.gateway.IOrderGateway

import org.springframework.stereotype.Component

@Component
class OrderController(
  private val orderConfig: OrderConfig
) {

  fun createOrder(
    orderGateway: IOrderGateway,
    orderRequest: OrderDTO,
    paymentClient: IPaymentWebhookClient
  ): OrderAdapter {
    val response = orderConfig.orderUseCase().createOrder(orderGateway, orderRequest, paymentClient)
    return OrderAdapter.fromOrderToSummary(response)
  }

  fun getOrder (orderGateway: IOrderGateway, id: String): OrderGetAdapter? {
    val response = orderConfig.orderUseCase().getOrder(orderGateway, id)
    return OrderGetAdapter.fromOrder(response)
  }

  fun getOrders(orderGateway: IOrderGateway): List<OrderGetAdapter?>? {
    val response = orderConfig.orderUseCase().getOrders(orderGateway)
    return response?.map { OrderGetAdapter.fromOrder(it) }
  }

  fun getOrderByStatus(orderGateway: IOrderGateway, string: String) : List<OrderGetAdapter>? {
    return orderConfig.orderUseCase().getOrderByStatus(orderGateway, string)?.map { OrderGetAdapter.fromOrder(it)!! }
  }
  fun deleteOrder(orderGateway: IOrderGateway, id: String) = orderConfig.orderUseCase().deleteOrder(
    orderGateway, id
  )

  fun updatePaymentOrder(orderGateway: IOrderGateway, paymentDTO: PaymentDTO) =
    orderConfig.orderUseCase().updatePaymentOrder(orderGateway, paymentDTO)
}