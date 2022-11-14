package io.oath.jwt

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import eu.timepit.refined.types.string.NonEmptyString
import io.oath.jwt.NestedHeader._
import io.oath.jwt.NestedPayload._
import io.oath.jwt.config.IssuerConfig
import io.oath.jwt.model.{IssueJwtError, JwtClaims, RegisteredClaims}
import io.oath.jwt.testkit.{AnyWordSpecBase, PropertyBasedTesting}
import io.oath.jwt.utils.ClockHelper

import scala.util.Try

import cats.implicits.catsSyntaxEitherId
import cats.implicits.catsSyntaxOptionId
import cats.implicits.toTraverseOps
import scala.concurrent.duration.DurationInt
import scala.jdk.CollectionConverters.ListHasAsScala
import scala.util.chaining.scalaUtilChainingOps

class JwtIssuerSpec extends AnyWordSpecBase with PropertyBasedTesting with ClockHelper {

  val jwtVerifier = JWT
    .require(Algorithm.none())
    .acceptLeeway(1)
    .build()

  "JwtIssuer" should {

    "issue jwt tokens" when {

      "issue token with predefine configure claims" in forAll { config: IssuerConfig =>
        val jwtIssuer = new JwtIssuer(config, clock)
        val jwtClaims = jwtIssuer.issueJwt().value

        val decodedJWT = jwtVerifier.verify(jwtClaims.token.value)

        Option(decodedJWT.getIssuer).flatMap(NonEmptyString.unapply) shouldBe config.registered.issuerClaim
        Option(decodedJWT.getSubject).flatMap(NonEmptyString.unapply) shouldBe config.registered.subjectClaim
        Option(decodedJWT.getAudience)
          .map(_.asScala.toSeq)
          .sequence
          .flatten
          .flatMap(NonEmptyString.unapply) shouldBe config.registered.audienceClaims

        Try(decodedJWT.getIssuedAt.toInstant).toOption shouldBe Option.when(config.registered.includeIssueAtClaim)(now)

        if (config.registered.includeJwtIdClaim)
          Option(decodedJWT.getId) should not be empty
        else
          Option(decodedJWT.getId) shouldBe empty

        Try(decodedJWT.getExpiresAt.toInstant).toOption shouldBe config.registered.expiresAtOffset.map(offset =>
          now.plusSeconds(offset.toSeconds))

        Try(decodedJWT.getNotBefore.toInstant).toOption shouldBe config.registered.notBeforeOffset.map(offset =>
          now.plusSeconds(offset.toSeconds))
      }

      "issue token with predefine configure claims and ad-hoc registered claims" in forAll {
        (registeredClaims: RegisteredClaims, config: IssuerConfig) =>
          val jwtIssuer = new JwtIssuer(config, clock)
          val jwtClaims = jwtIssuer.issueJwt(JwtClaims.Claims(registeredClaims)).value

          val expectedIssuer  = registeredClaims.iss orElse config.registered.issuerClaim
          val expectedSubject = registeredClaims.sub orElse config.registered.subjectClaim
          val expectedAudience =
            if (registeredClaims.aud.nonEmpty) registeredClaims.aud else config.registered.audienceClaims
          val expectedIssuedAt = registeredClaims.iat orElse Option.when(config.registered.includeIssueAtClaim)(now)
          val expectedExpiredAt =
            registeredClaims.exp orElse config.registered.expiresAtOffset.map(offset =>
              now.plusSeconds(offset.toSeconds))
          val expectedNotBefore =
            registeredClaims.nbf orElse config.registered.notBeforeOffset.map(offset =>
              now.plusSeconds(offset.toSeconds))

          jwtClaims.claims.registered.iss shouldBe expectedIssuer
          jwtClaims.claims.registered.sub shouldBe expectedSubject
          jwtClaims.claims.registered.aud shouldBe expectedAudience
          jwtClaims.claims.registered.iat shouldBe expectedIssuedAt
          jwtClaims.claims.registered.exp shouldBe expectedExpiredAt
          jwtClaims.claims.registered.nbf shouldBe expectedNotBefore

          if (registeredClaims.jti.nonEmpty)
            jwtClaims.claims.registered.jti shouldBe registeredClaims.jti
          else if (config.registered.includeJwtIdClaim)
            jwtClaims.claims.registered.jti should not be empty
          else jwtClaims.claims.registered.jti shouldBe empty
      }

      "issue token with registered claims when decoded should have the same values with the return registered claims" in forAll {
        (registeredClaims: RegisteredClaims, config: IssuerConfig) =>
          val adHocRegisteredClaims =
            registeredClaims.copy(iat = now.some, exp = now.plusSeconds(5.minutes.toSeconds).some, nbf = now.some)
          val jwtIssuer = new JwtIssuer(config, clock)
          val jwtClaims = jwtIssuer.issueJwt(JwtClaims.Claims(adHocRegisteredClaims)).value

          val decodedJWT = jwtVerifier.verify(jwtClaims.token.value)

          Option(decodedJWT.getIssuer).flatMap(NonEmptyString.unapply) shouldBe jwtClaims.claims.registered.iss
          Option(decodedJWT.getSubject).flatMap(NonEmptyString.unapply) shouldBe jwtClaims.claims.registered.sub
          Option(decodedJWT.getAudience)
            .map(_.asScala.toSeq)
            .sequence
            .flatten
            .flatMap(NonEmptyString.unapply) shouldBe jwtClaims.claims.registered.aud
          Try(decodedJWT.getIssuedAt.toInstant).toOption shouldBe jwtClaims.claims.registered.iat
          Option(decodedJWT.getId).flatMap(NonEmptyString.unapply) shouldBe jwtClaims.claims.registered.jti
          Try(decodedJWT.getExpiresAt.toInstant).toOption shouldBe jwtClaims.claims.registered.exp
          Try(decodedJWT.getNotBefore.toInstant).toOption shouldBe jwtClaims.claims.registered.nbf
      }

      "issue token with header claims" in forAll { (config: IssuerConfig, header: NestedHeader) =>
        val jwtIssuer = new JwtIssuer(config)
        val jwt       = jwtIssuer.issueJwt(JwtClaims.ClaimsH(header)).value

        val result = jwtVerifier
          .verify(jwt.token.value)
          .pipe(_.getHeaderClaim(dataField).asString())
          .pipe(nestedHeaderDecoder.decode)
          .value

        result shouldBe header
      }

      "issue token with payload claims" in forAll { (config: IssuerConfig, payload: NestedPayload) =>
        val jwtIssuer = new JwtIssuer(config)
        val jwt       = jwtIssuer.issueJwt(JwtClaims.ClaimsP(payload)).value

        val result = jwtVerifier
          .verify(jwt.token.value)
          .pipe(_.getClaim(dataField).asString())
          .pipe(nestedPayloadDecoder.decode)
          .value

        result shouldBe payload
      }

      "issue token with header & payload claims" in forAll {
        (config: IssuerConfig, header: NestedHeader, payload: NestedPayload) =>
          val jwtIssuer = new JwtIssuer(config)
          val jwt       = jwtIssuer.issueJwt(JwtClaims.ClaimsHP(header, payload)).value

          val (headerResult, payloadResult) = jwtVerifier
            .verify(jwt.token.value)
            .pipe(decodedJwt =>
              decodedJwt.getHeaderClaim(dataField).asString() -> decodedJwt.getClaim(dataField).asString())
            .pipe { case (headerJson, payloadJson) =>
              (nestedHeaderDecoder.decode(headerJson).value, nestedPayloadDecoder.decode(payloadJson).value)
            }

          headerResult shouldBe header
          payloadResult shouldBe payload
      }

      "issue token should fail with IllegalArgument when algorithm is set to null" in forAll { config: IssuerConfig =>
        val jwtIssuer = new JwtIssuer(config.copy(algorithm = null))
        val jwt       = jwtIssuer.issueJwt()

        jwt shouldBe IssueJwtError.IllegalArgument("The Algorithm cannot be null.").asLeft
      }
    }
  }
}
