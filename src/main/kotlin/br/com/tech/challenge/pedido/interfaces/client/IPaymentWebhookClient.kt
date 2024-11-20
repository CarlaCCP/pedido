package br.com.tech.challenge.pedido.interfaces.client

import br.com.tech.challenge.pedido.core.dto.PaymentDTO
import br.com.tech.challenge.pedido.core.valueObject.payment.PaymentEvent
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod

//@FeignClient(url = "svc-payment", value = "payment")
@FeignClient(url = "localhost:8081", value = "payment")
interface IPaymentWebhookClient {

  @RequestMapping(method = [RequestMethod.POST], value = ["/payment/create/{id}"])
  fun createPayment(@PathVariable id: String): PaymentEvent

  @RequestMapping(method = [RequestMethod.GET], value = ["/payment/{id}"])
  fun getPayment(@PathVariable id: String) : PaymentDTO?
}