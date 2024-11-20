package br.com.tech.challenge.pedido.config

import br.com.tech.challenge.pedido.core.valueObject.status.Status
import br.com.tech.challenge.pedido.interfaces.gateway.IOrderGateway
import br.com.tech.challenge.pedido.usecase.OrderUseCase
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.Scheduled

@Configuration
class OrderConfig(
  private val orderGateway: IOrderGateway,
) {
  @Bean
  fun orderUseCase(): OrderUseCase {
    return OrderUseCase()
  }

  @Scheduled(fixedDelay = 120000)
  fun deleteOrderByStatus() {
    OrderUseCase().deleteOrderByStatus(orderGateway)
  }

  @Scheduled(fixedDelay = 120000)
  fun updateStatusPreparacao() {
    OrderUseCase().updateOrderByStatus(orderGateway, Status.EM_PREPARACAO)
  }

  @Scheduled(fixedDelay = 120000)
  fun updateStatusFinalizado() {
    OrderUseCase().updateOrderByStatus(orderGateway, Status.PRONTO)
  }

  @Scheduled(fixedDelay = 120000)
  fun updateStatusRecebido() {
    OrderUseCase().updateOrderByStatus(orderGateway, Status.RECEBIDO)
  }
}
