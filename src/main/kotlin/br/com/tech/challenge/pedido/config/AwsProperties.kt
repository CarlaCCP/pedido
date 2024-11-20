package br.com.tech.challenge.pedido.config

import org.springframework.boot.context.properties.ConfigurationProperties


@ConfigurationProperties("aws")
data class AwsProperties (

  val accessKey: String,
  val secretKey: String,
  val sessionToken: String,
//  val region: String
){
}