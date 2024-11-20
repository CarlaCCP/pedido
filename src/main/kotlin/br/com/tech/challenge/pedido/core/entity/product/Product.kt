package br.com.tech.challenge.pedido.core.entity.product


import br.com.tech.challenge.pedido.core.valueObject.category.Category
import java.util.*

data class Product(
  var id: String? = UUID.randomUUID().toString(),
  var categoria: Category,
  var nome: String,
  var descricao: String,
  var preco: Double,
  var imagem: String
)