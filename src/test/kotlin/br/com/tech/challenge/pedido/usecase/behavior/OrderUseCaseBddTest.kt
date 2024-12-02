package br.com.tech.challenge.pedido.usecase.behavior

import br.com.tech.challenge.pedido.adapter.OrderGetAdapter
import br.com.tech.challenge.pedido.core.dto.OrderDTO
import br.com.tech.challenge.pedido.core.dto.ProductDTO
import br.com.tech.challenge.pedido.core.entity.order.Order
import br.com.tech.challenge.pedido.core.entity.product.Product
import br.com.tech.challenge.pedido.core.valueObject.category.Category
import br.com.tech.challenge.pedido.core.valueObject.payment.PaymentEvent
import br.com.tech.challenge.pedido.core.valueObject.status.Status
import br.com.tech.challenge.pedido.interfaces.client.IPaymentWebhookClient
import br.com.tech.challenge.pedido.interfaces.gateway.IOrderGateway
import br.com.tech.challenge.pedido.usecase.OrderUseCase
import com.amazonaws.services.dynamodbv2.document.DeleteItemOutcome
import com.amazonaws.services.dynamodbv2.model.DeleteItemResult
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDateTime

@Tags("behavior")
class OrderUseCaseBddTest : BehaviorSpec({

  lateinit var orderGateway: IOrderGateway
  lateinit var paymentClient: IPaymentWebhookClient
  lateinit var orderUseCase: OrderUseCase


  beforeTest {
    orderGateway = mockk<IOrderGateway>(relaxed = true)
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


  given("Um pedido") {

    `when`("Quando precisar trazer um pedido") {

      then("Deverá consultar por id e retornar") {
        every { orderGateway.findById("1") } returns order

        shouldNotBeNull {
          orderUseCase.getOrder(orderGateway, "1")
        }
      }

      then("Deverá consultar por status e retornar") {
        every { orderGateway.findByStatus(Status.EM_PREPARACAO) } returns listOf(order)

        shouldNotBeNull {
          orderUseCase.getOrderByStatus(orderGateway, Status.EM_PREPARACAO.description)
        }
      }

    }


    `when`("Quando cadastrar") {

      then("Deverá receber um pedido e salvar quando o status do pagamento é criado") {
        every { orderGateway.save(order) } returns order
        every { paymentClient.createPayment("1") } returns PaymentEvent.CREATED

        shouldNotBeNull {
          orderUseCase.createOrder(orderGateway, orderRequest, paymentClient)
        }
      }

      then("Não deverá salvar um pedido quando pagamento é negado") {
        every { orderGateway.save(order) } returns order
        every { paymentClient.createPayment("1") } returns PaymentEvent.DENIED
        every { orderGateway.save(order.copy(orderStatus = Status.NEGADO)) } returns order

        shouldNotBeNull {
          orderUseCase.createOrder(orderGateway, orderRequest, paymentClient)
        }
      }

      then("Deverá criar um pedido quando o pgamento é aprovado") {
        every { orderGateway.save(order) } returns order
        every { paymentClient.createPayment("1") } returns PaymentEvent.APPROVED

        shouldNotBeNull {
          orderUseCase.createOrder(orderGateway, orderRequest, paymentClient)
        }
      }

    }


    `when`("Quando deletar") {

      then("Deve receber o id e deletar o pedido") {
        every { orderGateway.deleteById("1") } returns DeleteItemOutcome(DeleteItemResult())

        shouldNotBeNull {
          orderUseCase.deleteOrder(orderGateway, "1")
        }
      }

    }

  }

})