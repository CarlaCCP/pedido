package br.com.tech.challenge.pedido.core.valueObject.status

enum class Status(val description: String) {
  RECEBIDO("Recebido"),
  EM_PREPARACAO("Em preparação"),
  PRONTO("Pronto"),
  FINALIZADO("Finalizado"),
  NEGADO("Pagamento negado"),
  AGUARDANDO_PAGAMENTO("Aguardando pagamento")
  ;

  companion object {

    fun getByDescription(description: String) =
        entries.firstOrNull {
          description == it.description
        }
  }
}