package br.com.tech.challenge.pedido.config


import br.com.tech.challenge.pedido.interfaces.gateway.IOrderGateway
import com.amazonaws.auth.*
import com.amazonaws.regions.Regions
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import org.socialsignin.spring.data.dynamodb.repository.config.EnableDynamoDBRepositories
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableDynamoDBRepositories(
  basePackageClasses = [
    IOrderGateway::class
  ]
)
class DynamoDBConfig(private val awsProperties: AwsProperties) {
  @Bean
  fun amazonDynamoDB(): AmazonDynamoDB = AmazonDynamoDBClientBuilder
    .standard()
    .withRegion(Regions.US_EAST_1)
    .withCredentials(awsCredentialsProvider())
    .build()

  private fun awsCredentialsProvider(): AWSCredentialsProvider {
    return AWSStaticCredentialsProvider(awsCredentials())
  }


  private fun awsCredentials(): AWSCredentials {
    println(awsProperties.accessKey)
    println(awsProperties.secretKey)
    println(awsProperties.sessionToken)
    return BasicSessionCredentials(
      awsProperties.accessKey,
      awsProperties.secretKey,
      awsProperties.sessionToken
    )
  }


}