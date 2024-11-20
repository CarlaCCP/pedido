package br.com.tech.challenge.pedido.interfaces.db

import br.com.tech.challenge.pedido.core.entity.product.Product
import br.com.tech.challenge.pedido.core.valueObject.status.Status
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable
import org.springframework.data.annotation.Id
import java.time.LocalDateTime

@DynamoDBTable(tableName = "pedido")
interface IOrder {
  @get:DynamoDBHashKey
  @get:Id
  var id: String?
  @get:DynamoDBAttribute
  var products: List<Product>
  @get:DynamoDBAttribute
  var createdAt: LocalDateTime?
  @get:DynamoDBAttribute
  var preco: Double
  @get:DynamoDBAttribute
  var orderStatus: Status?
}