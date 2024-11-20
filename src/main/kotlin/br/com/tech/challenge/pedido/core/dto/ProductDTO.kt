package br.com.tech.challenge.pedido.core.dto

import br.com.tech.challenge.pedido.core.entity.product.Product
import br.com.tech.challenge.pedido.core.valueObject.category.Category

data class ProductDTO(
    var id: String? = null,
    var categoria: String,
    var nome: String,
    var descricao: String,
    var preco: Double,
    var imagem: String
) {

  companion object {
    fun fromProduct(product: Product) =
        ProductDTO(
            product.id,
            product.categoria.description,
            product.nome,
            product.descricao,
            product.preco,
            product.imagem
        )
  }

  fun toProduct() =
      Product(
          id = id,
          categoria = Category.getByDescription(categoria),
          nome = nome,
          descricao = descricao,
          preco = preco,
          imagem = imagem

      )
}