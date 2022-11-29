package io.oath.jwt

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import eu.timepit.refined.types.string.NonEmptyString
import io.oath.jwt.NestedHeader._
import io.oath.jwt.NestedPayload._
import io.oath.jwt.config.VerifierConfig
import io.oath.jwt.config.VerifierConfig.{LeewayWindowConfig, ProvidedWithConfig}
import io.oath.jwt.model.{JwtVerifyError, RegisteredClaims}
import io.oath.jwt.syntax._
import io.oath.jwt.testkit.{AnyWordSpecBase, PropertyBasedTesting}
import io.oath.jwt.utils._

import cats.implicits.catsSyntaxEitherId
import cats.implicits.catsSyntaxOptionId
import scala.util.chaining.scalaUtilChainingOps

class JwtVerifierSpec extends AnyWordSpecBase with PropertyBasedTesting with ClockHelper {

  val defaultConfig =
    VerifierConfig(Algorithm.none(), ProvidedWithConfig(None, None, Nil), LeewayWindowConfig(None, None, None, None))

  "JwtVerifier" should {

    "verify token with prerequisite configurations" in forAll { config: VerifierConfig =>
      val jwtVerifier = new JwtVerifier(config)

      val leeway    = config.leewayWindow.leeway.map(leeway => now.plusSeconds(leeway.toSeconds - 1))
      val expiresAt = config.leewayWindow.expiresAt.map(expiresAt => now.plusSeconds(expiresAt.toSeconds - 1))
      val notBefore = config.leewayWindow.notBefore.map(notBefore => now.plusSeconds(notBefore.toSeconds - 1))
      val issueAt   = config.leewayWindow.issuedAt.map(issueAt => now.plusSeconds(issueAt.toSeconds - 1))

      val token = JWT
        .create()
        .tap(builder => config.providedWith.issuerClaim.map(nonEmptyString => builder.withIssuer(nonEmptyString.value)))
        .tap(builder =>
          config.providedWith.subjectClaim.map(nonEmptyString => builder.withSubject(nonEmptyString.value)))
        .tap(builder => builder.withAudience(config.providedWith.audienceClaims.map(_.value): _*))
        .tap(builder => (expiresAt orElse leeway).map(builder.withExpiresAt))
        .tap(builder => (notBefore orElse leeway).map(builder.withNotBefore))
        .tap(builder => (issueAt orElse leeway).map(builder.withIssuedAt))
        .sign(config.algorithm)

      val verified = jwtVerifier.verifyJwt(NonEmptyString.unsafeFrom(token).toToken).value

      verified.registered shouldBe RegisteredClaims(
        config.providedWith.issuerClaim,
        config.providedWith.subjectClaim,
        config.providedWith.audienceClaims,
        expiresAt orElse leeway,
        notBefore orElse leeway,
        issueAt orElse leeway,
        None
      )
    }

    "verify a token with header" in forAll { nestedHeader: NestedHeader =>
      val token = JWT
        .create()
        .withHeader(unsafeParseJsonToJavaMap(nestedHeaderEncoder.encode(nestedHeader)))
        .sign(defaultConfig.algorithm)

      val jwtVerifier = new JwtVerifier(defaultConfig)
      val verified    = jwtVerifier.verifyJwt[NestedHeader](NonEmptyString.unsafeFrom(token).toTokenH)

      verified.value shouldBe nestedHeader.toClaimsH
    }

    "verify a token with payload" in forAll { nestedPayload: NestedPayload =>
      val token = JWT
        .create()
        .withPayload(unsafeParseJsonToJavaMap(nestedPayloadEncoder.encode(nestedPayload)))
        .sign(defaultConfig.algorithm)

      val jwtVerifier = new JwtVerifier(defaultConfig)
      val verified    = jwtVerifier.verifyJwt[NestedPayload](NonEmptyString.unsafeFrom(token).toTokenP)

      verified.value shouldBe nestedPayload.toClaimsP
    }

    "verify a token with header & payload" in forAll { (nestedPayload: NestedPayload, nestedHeader: NestedHeader) =>
      val token = JWT
        .create()
        .withPayload(unsafeParseJsonToJavaMap(nestedPayloadEncoder.encode(nestedPayload)))
        .withHeader(unsafeParseJsonToJavaMap(nestedHeaderEncoder.encode(nestedHeader)))
        .sign(defaultConfig.algorithm)

      val jwtVerifier = new JwtVerifier(defaultConfig)
      val verified =
        jwtVerifier.verifyJwt[NestedHeader, NestedPayload](NonEmptyString.unsafeFrom(token).toTokenHP)

      verified.value shouldBe (nestedHeader, nestedPayload).toClaimsHP
    }

    "fail to decode a token with header" in {
      val header = """{"name": "name"}"""
      val token = JWT
        .create()
        .withHeader(unsafeParseJsonToJavaMap(header))
        .sign(defaultConfig.algorithm)

      val jwtVerifier = new JwtVerifier(defaultConfig)
      val verified    = jwtVerifier.verifyJwt[NestedHeader](NonEmptyString.unsafeFrom(token).toTokenH)

      verified shouldBe Left(JwtVerifyError.DecodingError("Missing required field: DownField(mapping)", null))
    }

    "fail to decode a token with payload" in {
      val payload = """{"name": "name"}"""
      val token = JWT
        .create()
        .withPayload(unsafeParseJsonToJavaMap(payload))
        .sign(defaultConfig.algorithm)

      val jwtVerifier = new JwtVerifier(defaultConfig)
      val verified    = jwtVerifier.verifyJwt[NestedPayload](NonEmptyString.unsafeFrom(token).toTokenP)

      verified shouldBe Left(JwtVerifyError.DecodingError("Missing required field: DownField(mapping)", null))
    }

    "fail to decode a token with header & payload" in {
      val header  = """{"name": "name"}"""
      val payload = """{"name": "name"}"""
      val token = JWT
        .create()
        .withHeader(unsafeParseJsonToJavaMap(header))
        .withPayload(unsafeParseJsonToJavaMap(payload))
        .sign(defaultConfig.algorithm)

      val jwtVerifier = new JwtVerifier(defaultConfig)
      val verified =
        jwtVerifier.verifyJwt[NestedHeader, NestedPayload](NonEmptyString.unsafeFrom(token).toTokenHP)

      verified shouldBe Left(
        JwtVerifyError.DecodingErrors(
          JwtVerifyError.DecodingError("Missing required field: DownField(mapping)", null).some,
          JwtVerifyError.DecodingError("Missing required field: DownField(mapping)", null).some
        ))
    }

    "fail to decode a token with header if exception raised in decoder" in {
      val token = JWT
        .create()
        .sign(defaultConfig.algorithm)

      val jwtVerifier = new JwtVerifier(defaultConfig)
      val verified    = jwtVerifier.verifyJwt[SimpleHeader](NonEmptyString.unsafeFrom(token).toTokenH)

      verified.left.value.error shouldBe "Boom"
    }

    "fail to decode a token with payload if exception raised in decoder" in {
      val token = JWT
        .create()
        .sign(defaultConfig.algorithm)

      val jwtVerifier = new JwtVerifier(defaultConfig)
      val verified    = jwtVerifier.verifyJwt[SimplePayload](NonEmptyString.unsafeFrom(token).toTokenP)

      verified.left.value.error shouldBe "Boom"
    }

    "fail to decode a token with header & payload if exception raised in decoder" in {
      val token = JWT
        .create()
        .sign(defaultConfig.algorithm)

      val jwtVerifier = new JwtVerifier(defaultConfig)
      val verified =
        jwtVerifier.verifyJwt[SimpleHeader, SimplePayload](NonEmptyString.unsafeFrom(token).toTokenHP)

      verified.left.value.error shouldBe "JWT Failed to decode both parts: \nheader decoding error: Boom \npayload decoding error: Boom"
    }

    "fail to verify token with VerificationError when provided with claims are not meet criteria" in {
      val config = defaultConfig.copy(providedWith =
        defaultConfig.providedWith.copy(issuerClaim = Some(NonEmptyString.unsafeFrom("issuer"))))
      val token = JWT
        .create()
        .sign(config.algorithm)

      val jwtVerifier = new JwtVerifier(config)
      val verified    = jwtVerifier.verifyJwt(NonEmptyString.unsafeFrom(token).toToken)

      verified shouldBe JwtVerifyError.VerificationError("The Claim 'iss' is not present in the JWT.").asLeft
    }

    "fail to verify token with IllegalArgument when null algorithm is provided" in forAll { config: VerifierConfig =>
      val token = JWT
        .create()
        .sign(config.algorithm)

      val jwtVerifier = new JwtVerifier(config.copy(algorithm = null))

      val verified = jwtVerifier.verifyJwt(NonEmptyString.unsafeFrom(token).toToken)

      verified shouldBe JwtVerifyError.IllegalArgument("The Algorithm cannot be null.").asLeft
    }

    "fail to verify token with AlgorithmMismatch when jwt header algorithm doesn't match with verify" in forAll {
      config: VerifierConfig =>
        val token = JWT
          .create()
          .sign(config.algorithm)

        val jwtVerifier = new JwtVerifier(config.copy(algorithm = Algorithm.HMAC256("secret")))
        val verified    = jwtVerifier.verifyJwt(NonEmptyString.unsafeFrom(token).toToken)

        verified shouldBe
          JwtVerifyError
            .AlgorithmMismatch("The provided Algorithm doesn't match the one defined in the JWT's Header.")
            .asLeft
    }

    "fail to verify token with SignatureVerificationError when secrets provided are wrong" in forAll {
      config: VerifierConfig =>
        val token = JWT
          .create()
          .sign(Algorithm.HMAC256("secret1"))

        val jwtVerifier = new JwtVerifier(config.copy(algorithm = Algorithm.HMAC256("secret2")))
        val verified    = jwtVerifier.verifyJwt(NonEmptyString.unsafeFrom(token).toToken)

        verified shouldBe
          JwtVerifyError
            .SignatureVerificationError(
              "The Token's Signature resulted invalid when verified using the Algorithm: HmacSHA256")
            .asLeft
    }

    "fail to verify token with TokenExpired when JWT expires" in {
      val expiresAt = now.minusSeconds(1)
      val token = JWT
        .create()
        .withExpiresAt(expiresAt)
        .sign(defaultConfig.algorithm)

      val jwtVerifier = new JwtVerifier(defaultConfig)
      val verified    = jwtVerifier.verifyJwt(NonEmptyString.unsafeFrom(token).toToken)

      verified shouldBe
        JwtVerifyError
          .TokenExpired(s"The Token has expired on $expiresAt.")
          .asLeft
    }
  }
}
