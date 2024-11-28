package br.com.tech.challenge.pedido.api

import br.com.tech.challenge.pedido.adapter.OrderAdapter
import br.com.tech.challenge.pedido.adapter.OrderGetAdapter
import br.com.tech.challenge.pedido.controller.OrderController
import br.com.tech.challenge.pedido.core.dto.OrderDTO
import br.com.tech.challenge.pedido.core.dto.PaymentDTO
import br.com.tech.challenge.pedido.core.dto.ProductDTO
import br.com.tech.challenge.pedido.core.valueObject.payment.PaymentEvent
import br.com.tech.challenge.pedido.core.valueObject.status.Status
import br.com.tech.challenge.pedido.interfaces.client.IPaymentWebhookClient
import br.com.tech.challenge.pedido.interfaces.gateway.IOrderGateway
import com.amazonaws.services.dynamodbv2.document.DeleteItemOutcome
import com.amazonaws.services.dynamodbv2.model.DeleteItemResult
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDateTime

class OrderApiTest : FunSpec ({

  lateinit var orderGateway: IOrderGateway
  lateinit var orderController: OrderController
  lateinit var paymentClient: IPaymentWebhookClient

  lateinit var orderApi: OrderApi

  beforeTest {
    orderGateway = mockk<IOrderGateway>()
    orderController = mockk<OrderController>()
    paymentClient = mockk<IPaymentWebhookClient>()

    orderApi = OrderApi(
      orderGateway, orderController, paymentClient
    )
  }


  val productDto = ProductDTO(
    "1",
    "Lanche",
    "Lanche gostoso",
    "Lanche gostoso pra caramba",
    12.2,
    "url"
  )
  val orderRequest = OrderDTO(
    products = listOf(
      productDto
    )
  )

  val orderAdapter = OrderAdapter(
    id = "12",
    productsDescriptions = listOf(productDto).map { it.descricao },
    tempoEspera = LocalDateTime.now().toString(),
    preco = 12.2,
    status = Status.EM_PREPARACAO.name
  )

  val orderGetAdapter = OrderGetAdapter(products = orderRequest.products)
  val paymentEvent = PaymentDTO("1", PaymentEvent.CREATED)

  test("should get all orders") {
    every { orderController.getOrders(orderGateway) } returns listOf(orderGetAdapter)

    shouldNotBeNull {
      orderApi.getOrders()
    }
  }

  test("should get by id") {
    every { orderController.getOrder(orderGateway, "1") } returns orderGetAdapter

    shouldNotBeNull {
      orderApi.getOrder("1")
    }
  }

  test("should create order") {
    every { orderController.createOrder(orderGateway, orderRequest, paymentClient) } returns orderAdapter

    shouldNotBeNull {
      orderApi.createOrder(orderRequest)
    }
  }

  test("should get order by status") {
    every { orderController.getOrderByStatus(orderGateway, Status.RECEBIDO.name) } returns listOf(orderGetAdapter)

    shouldNotBeNull {
      orderApi.getByStatus(Status.RECEBIDO.name)
    }
  }


  test("should delete order") {
    every { orderController.deleteOrder(orderGateway, "1") } returns DeleteItemOutcome(DeleteItemResult())

    shouldNotBeNull {
      orderApi.deleteOrder("1")
    }
  }

  test("should update payment order") {
    every {
      orderController.updatePaymentOrder(orderGateway, paymentEvent)
    } returns paymentEvent

    shouldNotBeNull {
      orderApi.updatePaymentOrder(paymentEvent)
    }
  }
})