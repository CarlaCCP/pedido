package br.com.tech.challenge.pedido.gateway

import br.com.tech.challenge.pedido.adapter.OrderGetAdapter
import br.com.tech.challenge.pedido.core.dto.OrderDTO
import br.com.tech.challenge.pedido.core.dto.ProductDTO
import br.com.tech.challenge.pedido.core.entity.product.Product
import br.com.tech.challenge.pedido.core.valueObject.category.Category
import br.com.tech.challenge.pedido.core.valueObject.status.Status
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.document.Table
import com.amazonaws.services.dynamodbv2.model.*
import com.amazonaws.services.ssmincidents.model.AttributeValueList
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDateTime

class OrderGatewayImplTest : FunSpec({


  lateinit var dynamoDBClient: AmazonDynamoDB
  lateinit var dynamoDb: DynamoDB
  lateinit var table: Table
  lateinit var orderGatewayImpl: OrderGatewayImpl

  beforeTest {

    dynamoDBClient = mockk<AmazonDynamoDB>(relaxed = true)
    dynamoDb = mockk<DynamoDB>()
    table = mockk<Table>(relaxed = true)

    orderGatewayImpl = OrderGatewayImpl(dynamoDBClient)
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

  val product = Product(
    "1",
    Category.LANCHE,
    "Lanche gostoso",
    "Lanche gostoso pra caramba",
    12.2,
    "url"
  )
  val productList = listOf(product)

  val orderGetAdapter = OrderGetAdapter(products = orderRequest.products)
    .toPedido()

  val item = mapOf(
    "id" to AttributeValue().withS("1"),
    "createdAt" to AttributeValue().withS(LocalDateTime.now().toString()),
    "preco" to AttributeValue().withN(12.2.toString()),
    "orderStatus" to AttributeValue().withS(Status.EM_PREPARACAO.name),
    "products" to AttributeValue().withL(
      productList.map {
        AttributeValue().withM(
          mapOf(
            "categoria" to AttributeValue().withS(it.categoria.name),
            "nome" to AttributeValue().withS(it.nome),
            "descricao" to AttributeValue().withS(it.descricao),
            "preco" to AttributeValue().withN(it.preco.toString()),
            "imagem" to AttributeValue().withS(it.imagem),
          )
        )
      }
    )
  )

  test("should find all orders") {
    every {
      dynamoDBClient.scan(ScanRequest().withTableName("pedido"))
    } returns ScanResult().withItems(item)

    shouldNotBeNull {
      orderGatewayImpl.findAll()
    }

  }


  test("should find By Status") {
    every {
      dynamoDBClient.query(
        QueryRequest()
          .withTableName("pedido")
          .withIndexName("orderStatus-index")
          .withScanIndexForward(false)
          .withKeyConditionExpression("orderStatus = :orderStatus")
          .withExpressionAttributeValues(
            mapOf(
              ":orderStatus" to AttributeValue().withS(Status.EM_PREPARACAO.name)
            )
          )
      )
    } returns QueryResult().withItems(item)

    shouldNotBeNull {
      orderGatewayImpl.findByStatus(Status.EM_PREPARACAO)
    }

  }



  test("should find order By id") {
    every {
      dynamoDBClient.query(
        QueryRequest()
          .withTableName("pedido")
          .withKeyConditionExpression("id = :id")
          .withExpressionAttributeValues(mapOf(
            ":id" to AttributeValue().withS("1"))
          )
      )
    } returns QueryResult().withItems(item)

    shouldNotBeNull {
      orderGatewayImpl.findById("1")
    }

  }


  test("should save order") {
    every {
      dynamoDBClient.putItem(
        PutItemRequest()
          .withTableName("pedido")
          .withItem(item)
      )
    } returns PutItemResult()

    shouldNotBeNull {
      orderGatewayImpl.save(orderGetAdapter)
    }

  }


})