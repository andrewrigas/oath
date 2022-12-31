package io.oath.juror.config

import io.oath.jwt.config.{JwtIssuerConfig, JwtManagerConfig, JwtVerifierConfig}

private[juror] object ConfigLoader {

  def issuer(configLocation: String): JwtIssuerConfig =
    JwtIssuerConfig.loadOrThrow(rootConfig.getConfig(configLocation))

  def verifier(configLocation: String): JwtVerifierConfig =
    JwtVerifierConfig.loadOrThrow(rootConfig.getConfig(configLocation))

  def manager(configLocation: String): JwtManagerConfig =
    JwtManagerConfig.loadOrThrow(rootConfig.getConfig(configLocation))
}
