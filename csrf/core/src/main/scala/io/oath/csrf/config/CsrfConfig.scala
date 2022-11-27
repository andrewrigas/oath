package io.oath.csrf.config

import com.typesafe.config.{Config, ConfigFactory}
import eu.timepit.refined.types.string.NonEmptyString

import scala.util.chaining.scalaUtilChainingOps

final case class CsrfConfig(secret: NonEmptyString)

object CsrfConfig {
  private val CsrfConfigLocation   = "csrf"
  private val SecretKeyConfigValue = "secret-key"

  def loadOrThrow(config: Config): CsrfConfig =
    config
      .getConfig(CsrfConfigLocation)
      .getString(SecretKeyConfigValue)
      .pipe(NonEmptyString.unsafeFrom)
      .pipe(CsrfConfig(_))

  def loadOrThrow(location: String): CsrfConfig = {
    val configLocation = ConfigFactory.load().getConfig(location)
    loadOrThrow(configLocation)
  }
}
