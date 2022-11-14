package io.oath.jwt.config

import com.typesafe.config.{Config, ConfigFactory}

import scala.util.chaining.scalaUtilChainingOps

final case class ManagerConfig(issuer: IssuerConfig, verifier: VerifierConfig)

object ManagerConfig {

  def loadOrThrow(location: String): ManagerConfig = ConfigFactory.load().getConfig(location).pipe(loadOrThrow)

  def loadOrThrow(config: Config): ManagerConfig =
    ManagerConfig(IssuerConfig.loadOrThrow(config), VerifierConfig.loadOrThrow(config))
}
