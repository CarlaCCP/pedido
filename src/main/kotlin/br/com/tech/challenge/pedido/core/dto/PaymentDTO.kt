package br.com.tech.challenge.pedido.core.dto

import br.com.tech.challenge.pedido.core.valueObject.payment.PaymentEvent

data class PaymentDTO(
  val id: String,
  val event: PaymentEvent
)
