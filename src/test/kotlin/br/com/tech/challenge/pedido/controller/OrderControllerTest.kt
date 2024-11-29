package br.com.tech.challenge.pedido.controller

import br.com.tech.challenge.pedido.adapter.OrderGetAdapter
import br.com.tech.challenge.pedido.config.OrderConfig
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

class OrderControllerTest : FunSpec ({

  lateinit var orderGateway: IOrderGateway
  lateinit var paymentClient: IPaymentWebhookClient
  lateinit var orderConfig: OrderConfig

  lateinit var orderController: OrderController

  beforeTest {
    orderGateway = mockk<IOrderGateway>()
    paymentClient = mockk<IPaymentWebhookClient>()
    orderConfig = mockk<OrderConfig>()

    orderController = OrderController(orderConfig)

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
  val orderGetAdapter = OrderGetAdapter(products = orderRequest.products)
    .toPedido()
    .copy(
      id = "1",
      createdAt = LocalDateTime.parse("2024-11-27T14:06:49.481430"),
      orderStatus = Status.RECEBIDO
    )

  test("Should create order") {
    every {
      orderConfig
        .orderUseCase()
        .createOrder(
          orderGateway,
          orderRequest,
          paymentClient
        )
    } returns orderGetAdapter

    shouldNotBeNull {
      orderController.createOrder(
        orderGateway,
        orderRequest,
        paymentClient
      )
    }
  }

  test("Should get order") {
    every { orderConfig.orderUseCase().getOrder(orderGateway, "1")
    } returns orderGetAdapter

    shouldNotBeNull {
      orderController.getOrder(orderGateway, "1")
    }
  }


  test("Should get orders") {
    every { orderConfig.orderUseCase().getOrders(orderGateway)
    } returns listOf(orderGetAdapter)

    shouldNotBeNull {
      orderController.getOrders(orderGateway)
    }
  }

  test("Should get order by status") {
    every { orderConfig.orderUseCase().getOrderByStatus(orderGateway, Status.EM_PREPARACAO.name)
    } returns listOf(orderGetAdapter)

    shouldNotBeNull {
      orderController.getOrderByStatus(orderGateway, Status.EM_PREPARACAO.name)
    }
  }


  test("Should delete order") {
    every { orderConfig.orderUseCase().deleteOrder(orderGateway, "1")
    } returns DeleteItemOutcome(DeleteItemResult())

    shouldNotBeNull {
      orderController.deleteOrder(orderGateway, "1")
    }
  }

  test("Should update payment order") {
    every { orderConfig.orderUseCase().updatePaymentOrder(orderGateway,  PaymentDTO("1", PaymentEvent.CREATED))
    } returns  PaymentDTO("1", PaymentEvent.CREATED)

    shouldNotBeNull {
      orderController.updatePaymentOrder(orderGateway, PaymentDTO("1", PaymentEvent.CREATED))
    }
  }
})