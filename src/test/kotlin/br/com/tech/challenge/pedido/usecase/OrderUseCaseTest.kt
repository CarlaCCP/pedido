package br.com.tech.challenge.pedido.usecase

import br.com.tech.challenge.pedido.adapter.OrderGetAdapter
import br.com.tech.challenge.pedido.core.dto.OrderDTO
import br.com.tech.challenge.pedido.core.dto.PaymentDTO
import br.com.tech.challenge.pedido.core.dto.ProductDTO
import br.com.tech.challenge.pedido.core.entity.order.Order
import br.com.tech.challenge.pedido.core.entity.product.Product
import br.com.tech.challenge.pedido.core.valueObject.category.Category
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

class OrderUseCaseTest : FunSpec({

  lateinit var orderGateway: IOrderGateway
  lateinit var paymentClient: IPaymentWebhookClient
  lateinit var orderUseCase: OrderUseCase


  beforeTest {
    orderGateway = mockk<IOrderGateway>( relaxed = true)
    paymentClient = mockk<IPaymentWebhookClient>(relaxed = true)
    orderUseCase = OrderUseCase()

  }

  val product = Product(
    "1",
    Category.LANCHE,
    "Lanche gostoso",
    "Lanche gostoso pra caramba",
    12.2,
    "url"
  )

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

  val order = Order(
    orderGetAdapter.id,
    listOf(product),
    orderGetAdapter.createdAt,
    12.2,
    Status.RECEBIDO
  )



  test("Should get order by id") {
    every { orderGateway.findById("1") } returns order

    shouldNotBeNull {
      orderUseCase.getOrder(orderGateway, "1")
    }

  }

  test("Should get order by status") {
    every { orderGateway.findByStatus(Status.EM_PREPARACAO) } returns listOf(order)

    shouldNotBeNull {
      orderUseCase.getOrderByStatus(orderGateway, Status.EM_PREPARACAO.description)
    }
  }

  test("Should create order") {
    every { orderGateway.save(order) } returns order
    every { paymentClient.createPayment("1") } returns PaymentEvent.CREATED

    shouldNotBeNull {
      orderUseCase.createOrder(orderGateway, orderRequest, paymentClient)
    }
  }

  test("Should not create order with payment is denied") {
    every { orderGateway.save(order) } returns order
    every { paymentClient.createPayment("1") } returns PaymentEvent.DENIED
    every { orderGateway.save(order.copy(orderStatus = Status.NEGADO)) } returns order

    shouldNotBeNull {
      orderUseCase.createOrder(orderGateway, orderRequest, paymentClient)
    }
  }

  test("Should create order with payment is approved") {
    every { orderGateway.save(order) } returns order
    every { paymentClient.createPayment("1") } returns PaymentEvent.APPROVED

    shouldNotBeNull {
      orderUseCase.createOrder(orderGateway, orderRequest, paymentClient)
    }
  }


  test("Should delete order by id") {
    every { orderGateway.deleteById("1") } returns DeleteItemOutcome(DeleteItemResult())

    shouldNotBeNull {
      orderUseCase.deleteOrder(orderGateway, "1")
    }
  }


  test("Should get all orders") {
    every { orderGateway.findAll() } returns listOf(order)

    shouldNotBeNull {
      orderUseCase.getOrders(orderGateway)
    }
  }

  test("Should delete order by status") {
    every { orderGateway.findByStatus(Status.FINALIZADO) } returns listOf(order)
    every { orderGateway.deleteById("1") } returns DeleteItemOutcome(DeleteItemResult())

    shouldNotBeNull {
      orderUseCase.deleteOrderByStatus(orderGateway)
    }
  }

  test("Should update order by status") {
    every { orderGateway.findByStatus(Status.RECEBIDO) } returns listOf(order)

    every { orderGateway.save(orderGetAdapter) } returns orderGetAdapter

    shouldNotBeNull {
      orderUseCase.updateOrderByStatus(orderGateway, Status.RECEBIDO)
    }
  }

  test("Should update order by status EM PREPARAÇÃO") {
    every { orderGateway.findByStatus(Status.EM_PREPARACAO) } returns listOf(order.copy(orderStatus = Status.EM_PREPARACAO))

    every { orderGateway.save(orderGetAdapter) } returns orderGetAdapter

    shouldNotBeNull {
      orderUseCase.updateOrderByStatus(orderGateway, Status.EM_PREPARACAO)
    }
  }

  test("Should update order by status PRONTO") {
    every { orderGateway.findByStatus(Status.PRONTO) } returns listOf(order.copy(orderStatus = Status.PRONTO))

    every { orderGateway.save(orderGetAdapter.copy(orderStatus = Status.PRONTO)) } returns orderGetAdapter

    shouldNotBeNull {
      orderUseCase.updateOrderByStatus(orderGateway, Status.PRONTO)
    }
  }

  test("Should update payment order approved") {
    every { orderGateway.findById("1") } returns order
    every { orderGateway.save(orderGetAdapter) } returns orderGetAdapter

    shouldNotBeNull {
      orderUseCase.updatePaymentOrder(orderGateway, PaymentDTO("1", PaymentEvent.APPROVED))
    }
  }

  test("Should update payment order denied") {
    every { orderGateway.findById("1") } returns order
    every { orderGateway.save(orderGetAdapter) } returns orderGetAdapter

    shouldNotBeNull {
      orderUseCase.updatePaymentOrder(orderGateway, PaymentDTO("1", PaymentEvent.DENIED))
    }
  }

  test("Should update payment order created") {
    every { orderGateway.findById("1") } returns order
    every { orderGateway.save(orderGetAdapter) } returns orderGetAdapter

    shouldNotBeNull {
      orderUseCase.updatePaymentOrder(orderGateway, PaymentDTO("1", PaymentEvent.CREATED))
    }
  }


})