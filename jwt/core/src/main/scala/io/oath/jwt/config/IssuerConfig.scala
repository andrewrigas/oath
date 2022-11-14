package io.oath.jwt.config

import com.auth0.jwt.algorithms.Algorithm
import com.typesafe.config.{Config, ConfigFactory}
import eu.timepit.refined.types.string.NonEmptyString

import scala.concurrent.duration.FiniteDuration

import IssuerConfig.RegisteredConfig

final case class IssuerConfig(algorithm: Algorithm, registered: RegisteredConfig)

object IssuerConfig {

  final case class RegisteredConfig(issuerClaim: Option[NonEmptyString] = None,
                                    subjectClaim: Option[NonEmptyString] = None,
                                    audienceClaims: Seq[NonEmptyString] = Seq.empty,
                                    includeJwtIdClaim: Boolean = false,
                                    includeIssueAtClaim: Boolean = false,
                                    expiresAtOffset: Option[FiniteDuration] = None,
                                    notBeforeOffset: Option[FiniteDuration] = None
  )

  private val IssuerConfigLocation     = "issuer"
  private val AlgorithmConfigLocation  = "algorithm"
  private val RegisteredConfigLocation = "registered"

  private def loadOrThrowRegisteredConfig(registeredScoped: Config): RegisteredConfig = {
    val issuerClaim          = registeredScoped.getMaybeNonEmptyString("issuer-claim")
    val subjectClaim         = registeredScoped.getMaybeNonEmptyString("subject-claim")
    val audienceClaim        = registeredScoped.getSeqNonEmptyString("audience-claims")
    val includeIssuedAtClaim = registeredScoped.getBooleanDefaultFalse("include-issued-at-claim")
    val includeJwtIdClaim    = registeredScoped.getBooleanDefaultFalse("include-jwt-id-claim")
    val expiresAtOffset      = registeredScoped.getMaybeFiniteDuration("expires-at-offset")
    val notBeforeOffset      = registeredScoped.getMaybeFiniteDuration("not-before-offset")
    RegisteredConfig(
      issuerClaim,
      subjectClaim,
      audienceClaim,
      includeJwtIdClaim,
      includeIssuedAtClaim,
      expiresAtOffset,
      notBeforeOffset
    )
  }

  def loadOrThrow(config: Config): IssuerConfig = {
    val maybeIssuerScoped = config.getMaybeConfig(IssuerConfigLocation)
    val algorithm = AlgorithmLoader.loadAlgorithmOrThrow(config.getConfig(AlgorithmConfigLocation), forIssuing = true)
    val registered = maybeIssuerScoped
      .map(scoped => loadOrThrowRegisteredConfig(scoped.getConfig(RegisteredConfigLocation)))
      .getOrElse(RegisteredConfig())

    IssuerConfig(algorithm, registered)
  }

  def loadOrThrow(location: String): IssuerConfig = {
    val configLocation = ConfigFactory.load().getConfig(location)
    loadOrThrow(configLocation)
  }
}
