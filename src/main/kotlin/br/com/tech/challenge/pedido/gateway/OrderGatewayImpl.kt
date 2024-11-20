package br.com.tech.challenge.pedido.gateway

import br.com.tech.challenge.pedido.core.entity.order.Order
import br.com.tech.challenge.pedido.core.entity.product.Product
import br.com.tech.challenge.pedido.core.valueObject.category.Category
import br.com.tech.challenge.pedido.core.valueObject.status.Status
import br.com.tech.challenge.pedido.interfaces.gateway.IOrderGateway
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.document.DeleteItemOutcome
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.document.Item
import com.amazonaws.services.dynamodbv2.document.Table
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.amazonaws.services.dynamodbv2.model.DeleteItemRequest
import com.amazonaws.services.dynamodbv2.model.PutItemRequest
import com.amazonaws.services.dynamodbv2.model.QueryRequest
import com.amazonaws.services.dynamodbv2.model.ScanRequest
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class OrderGatewayImpl(
  private val dynamoDB: AmazonDynamoDB
) : IOrderGateway {


  private val tableName = "pedido"
  private val dynamoDBClient = DynamoDB(dynamoDB)
  private val table: Table? = dynamoDBClient.getTable(tableName)

  override fun findAll(): List<Order>? {
    val scanRequest = ScanRequest()
      .withTableName(tableName)

  val response = dynamoDB.scan(scanRequest)

      return response.items.map {
        Order(
          id = it["id"]?.s,
          products = it["products"]?.l!!.map { product ->
            Product(
              id = product.m["id"]?.s ?: "",
              categoria = Category.valueOf(product.m["categoria"]?.s ?: ""),
              descricao = product.m["descricao"]?.s ?: "",
              imagem = product.m["imagem"]?.s ?: "",
              nome = product.m["nome"]?.s ?: "",
              preco = product.m["preco"]?.n?.toDouble() ?: 0.0
            )
          },
          createdAt = LocalDateTime.parse(it["createdAt"]?.s ?: ""),
          preco = it["preco"]?.n?.toDouble() ?: 0.0,
          orderStatus = Status.valueOf(it["orderStatus"]?.s ?: "")
        )
      }

  }

  override fun findFirst(): Order? {
    val querySpec = QuerySpec()
      .withScanIndexForward(false)
    return table?.query(querySpec)?.map { itemToOrder(it) }?.first()

//    return mongoTemplate.findOne(
//      Query.query(
//        Criteria.where("status").exists(true)
//      ).with(Sort.by(Sort.Direction.DESC, "createAt")),
//      Order::class.java
//    )
  }

  override fun findByStatus(status: Status): List<Order>? {
    val queryRequest = QueryRequest()
      .withTableName(tableName)
      .withIndexName("orderStatus-index")
      .withScanIndexForward(false)
      .withKeyConditionExpression("orderStatus = :orderStatus")
      .withExpressionAttributeValues(
        mapOf(
          ":orderStatus" to AttributeValue().withS(status.name)
        )
      )
    val result = dynamoDB.query(queryRequest)
    return result.items.map {
      Order(
        id = it["id"]?.s,
        products = it["products"]?.l!!.map { product ->
          Product(
            id = product.m["id"]?.s ?: "",
            categoria = Category.valueOf(product.m["categoria"]?.s ?: ""),
            descricao = product.m["descricao"]?.s ?: "",
            imagem = product.m["imagem"]?.s ?: "",
            nome = product.m["nome"]?.s ?: "",
            preco = product.m["preco"]?.n?.toDouble() ?: 0.0
          )
        },
        createdAt = LocalDateTime.parse(it["createdAt"]?.s ?: ""),
        preco = it["preco"]?.n?.toDouble() ?: 0.0,
        orderStatus = Status.valueOf(it["orderStatus"]?.s ?: "")
      )
    }


//    val querySpec = QuerySpec()
//      .withKeyConditionExpression("status = :status")
//      .withScanIndexForward(false)
//      .withValueMap(mapOf(
//        ":status" to status
//      ))
//    return table?.query(querySpec)?.map { itemToOrder(it) }

//    return mongoTemplate.find(
//      Query.query(
//        Criteria.where("status").isEqualTo(status)
//      ).with(Sort.by(Sort.Direction.DESC, "createAt")),
//      Order::class.java
//    )
  }

  override fun findById(id: String): Order? {
    val queryRequest = QueryRequest()
      .withTableName(tableName)
      .withKeyConditionExpression("id = :id")
      .withExpressionAttributeValues(mapOf(
        ":id" to AttributeValue().withS(id))
      )
    val result = dynamoDB.query(queryRequest)
    return result.items.map {
      Order(
        id = it["id"]?.s,
        products = it["products"]?.l!!.map { product ->
          Product(
            id = product.m["id"]?.s ?: "",
            categoria = Category.valueOf(product.m["categoria"]?.s ?: ""),
            descricao = product.m["descricao"]?.s ?: "",
            imagem = product.m["imagem"]?.s ?: "",
            nome = product.m["nome"]?.s ?: "",
            preco = product.m["preco"]?.n?.toDouble() ?: 0.0
          )
        },
        createdAt = LocalDateTime.parse(it["createdAt"]?.s ?: ""),
        preco = it["preco"]?.n?.toDouble() ?: 0.0,
        orderStatus = Status.valueOf(it["orderStatus"]?.s ?: "")
      )
    }.firstOrNull()
  }

  override fun save(order: Order): Order {
    val item = mapOf(
      "id" to AttributeValue().withS(order.id),
      "createdAt" to AttributeValue().withS(order.createdAt.toString()),
      "preco" to AttributeValue().withN(order.preco.toString()),
      "orderStatus" to AttributeValue().withS(order.orderStatus?.name),
      "products" to AttributeValue().withL(
        order.products.map {
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
    val request = PutItemRequest()
      .withTableName(tableName)
      .withItem(item)

    dynamoDB.putItem(request)

    return order
//    val item = Item()
//      .withPrimaryKey("id", order.id)
//      .withList("products", order.products)
//      .with("createdAt", order.createdAt)
//      .withDouble("preco", order.preco)
//      .with("status", order.status)
//    table?.putItem(item)
//    return order
  }

  override fun deleteById(id: String): DeleteItemOutcome? {
    println(id)
    val request = DeleteItemRequest()
      .withTableName(tableName)
      .withKey(mapOf("id" to AttributeValue().withS(id)))
    dynamoDB.deleteItem(request)

    return table?.deleteItem("id", id)
  }

  private fun itemToOrder(item: Item) : Order{
    println(item.toJSON())
    println(item.toJSONPretty())
    println(item.attributes())
    return Order(
      id = item.getString("id"),
      products =  item.getList<Product?>("products").map {
        Product(
          categoria = it.categoria,
          descricao = it.descricao,
          imagem = it.imagem,
          nome = it.nome,
          preco = it.preco
        )
      },
      createdAt =  LocalDateTime.parse(item.getString("createdAt")),
      preco =  item.getDouble("preco"),
      orderStatus = Status.valueOf(item.getString("status")),
    )
  }


}