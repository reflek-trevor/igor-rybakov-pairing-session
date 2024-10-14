package io.reflek.support

import scala.jdk.CollectionConverters._

import com.typesafe.config.{ConfigFactory => TypesafeConfigFactory}
import com.typesafe.config.Config

object ConfigFactory {

  type Environment = Map[String, String]

  // we don't want to read the entire 'application.conf' here, so we use empty config
  def forUnitTesting(): Config = TypesafeConfigFactory.empty().withFallback(fromConfigFile("unit-test")).resolve()

  def forIntegrationTesting(environmentOverrides: Environment): Config =
    TypesafeConfigFactory
      .empty()
      .withFallback(fromEnvironment(environmentOverrides))
      .withFallback(fromConfigFile("integration-test"))
      .withFallback(TypesafeConfigFactory.defaultReference())
      .resolve()

  def fromConfigFile(file: String): Config =
    TypesafeConfigFactory.parseResourcesAnySyntax(file)

  def fromEnvironment(environment: Environment): Config =
    TypesafeConfigFactory.parseMap(environment.asJava)
}
