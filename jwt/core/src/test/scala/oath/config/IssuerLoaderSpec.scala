package oath.config

import com.typesafe.config.ConfigFactory
import eu.timepit.refined.types.string.NonEmptyString
import oath.testkit.{AnyWordSpecBase, PropertyBasedTesting}

import cats.implicits.catsSyntaxOptionId
import scala.concurrent.duration.DurationInt

class IssuerLoaderSpec extends AnyWordSpecBase with PropertyBasedTesting {

  val configFile                            = "issuer"
  val DefaultTokenConfigLocation            = "default-token"
  val TokenConfigLocation                   = "token"
  val InvalidTokenEmptyStringConfigLocation = "invalid-token-empty-string"
  val InvalidTokenWrongTypeConfigLocation   = "invalid-token-wrong-type"

  "IssuerLoader" should {

    "load default-token issuer config values from configuration file" in {
      val configLoader = ConfigFactory.load(configFile).getConfig(DefaultTokenConfigLocation)

      val config = IssuerConfig.loadOrThrow(configLoader)
      config.registered.issuerClaim shouldBe None
      config.registered.subjectClaim shouldBe None
      config.registered.audienceClaims shouldBe Seq.empty
      config.registered.includeIssueAtClaim shouldBe false
      config.registered.includeJwtIdClaim shouldBe false
      config.registered.expiresAtOffset shouldBe None
      config.registered.notBeforeOffset shouldBe None
      config.algorithm.getName shouldBe "HS256"
    }

    "load token issuer config values from configuration file" in {
      val configLoader = ConfigFactory.load(configFile).getConfig(TokenConfigLocation)

      val config = IssuerConfig.loadOrThrow(configLoader)
      config.registered.issuerClaim shouldBe NonEmptyString.unapply("issuer")
      config.registered.subjectClaim shouldBe NonEmptyString.unapply("subject")
      config.registered.audienceClaims shouldBe Seq("aud1","aud2").map(NonEmptyString.unsafeFrom)
      config.registered.includeIssueAtClaim shouldBe true
      config.registered.includeJwtIdClaim shouldBe false
      config.registered.expiresAtOffset shouldBe 1.day.some
      config.registered.notBeforeOffset shouldBe 1.minute.some
      config.algorithm.getName shouldBe "RS256"
    }

    "load token issuer config values from reference configuration file using location" in {
      val config = IssuerConfig.loadOrThrow(TokenConfigLocation)
      config.registered.issuerClaim shouldBe NonEmptyString.unapply("issuer")
      config.registered.subjectClaim shouldBe NonEmptyString.unapply("subject")
      config.registered.audienceClaims shouldBe Seq("aud1","aud2").map(NonEmptyString.unsafeFrom)
      config.registered.includeIssueAtClaim shouldBe true
      config.registered.includeJwtIdClaim shouldBe false
      config.registered.expiresAtOffset shouldBe 1.day.some
      config.registered.notBeforeOffset shouldBe 1.minute.some
      config.algorithm.getName shouldBe "RS256"
    }

    "load invalid-token-empty-string issuer config values from configuration file" in {
      val configLoader = ConfigFactory.load(configFile).getConfig(InvalidTokenEmptyStringConfigLocation)
      val x = IssuerConfig.loadOrThrow(configLoader)
      println(x.registered.issuerClaim)
      the[RuntimeException] thrownBy IssuerConfig.loadOrThrow(configLoader)
    }

    "load invalid-token-wrong-type issuer config values from configuration file" in {
      val configLoader = ConfigFactory.load(configFile).getConfig(InvalidTokenWrongTypeConfigLocation)
      val x = IssuerConfig.loadOrThrow(configLoader)
      println(x.registered.notBeforeOffset)
      the[RuntimeException] thrownBy IssuerConfig.loadOrThrow(configLoader)
    }
  }
}
