package io.oath.jwt.jsoniter

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import eu.timepit.refined.types.string.NonEmptyString
import io.oath.jwt.config.IssuerConfig.RegisteredConfig
import io.oath.jwt.config.VerifierConfig.{LeewayWindowConfig, ProvidedWithConfig}
import io.oath.jwt.config.{IssuerConfig, VerifierConfig}
import io.oath.jwt.model.{JwtClaims, JwtToken, JwtVerifyError}
import io.oath.jwt.testkit.AnyWordSpecBase
import io.oath.jwt.utils._
import io.oath.jwt.{JwtIssuer, JwtVerifier}

import scala.util.chaining.scalaUtilChainingOps

class JsoniterConversionSpec extends AnyWordSpecBase {

  val verifierConfig =
    VerifierConfig(Algorithm.none(), ProvidedWithConfig(None, None, Nil), LeewayWindowConfig(None, None, None, None))
  val issuerConfig =
    IssuerConfig(Algorithm.none(),
                 RegisteredConfig(None, None, Nil, includeJwtIdClaim = false, includeIssueAtClaim = false, None, None))

  val jwtVerifier = new JwtVerifier(verifierConfig)
  val jwtIssuer   = new JwtIssuer(issuerConfig)

  "JsoniterConversion" should {

    "convert jsoniter codec to claims (encoders & decoders)" in {
      val bar    = Bar("bar", 10)
      val jwt    = jwtIssuer.issueJwt(JwtClaims.ClaimsP(bar)).value
      val claims = jwtVerifier.verifyJwt[Bar](JwtToken.TokenP(jwt.token)).value

      claims.payload shouldBe bar
    }

    "convert jsoniter codec to claims decoder and get error" in {
      val fooJson = """{"name":"Hello","age":"not number"}"""
      val jwt = JWT
        .create()
        .withPayload(unsafeParseJsonToJavaMap(fooJson))
        .sign(Algorithm.none())
        .pipe(NonEmptyString.unsafeFrom)
      val claims = jwtVerifier.verifyJwt[Bar](JwtToken.TokenP(jwt))

      val decodingError: JwtVerifyError = claims.left.value
      decodingError.error should startWith("illegal number, offset: 0x00000016, buf:")
    }
  }
}
