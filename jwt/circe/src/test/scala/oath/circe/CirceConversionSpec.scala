package oath.circe

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import eu.timepit.refined.types.string.NonEmptyString
import oath.config.IssuerConfig.RegisteredConfig
import oath.config.VerifierConfig.{LeewayWindowConfig, ProvidedWithConfig}
import oath.config.{IssuerConfig, VerifierConfig}
import oath.model.{JwtClaims, JwtToken, JwtVerifyError}
import oath.testkit.AnyWordSpecBase
import oath.{dataField, JwtIssuer, JwtVerifier}

import scala.jdk.CollectionConverters.MapHasAsJava
import scala.util.chaining.scalaUtilChainingOps

class CirceConversionSpec extends AnyWordSpecBase {

  val verifierConfig =
    VerifierConfig(Algorithm.none(), ProvidedWithConfig(None, None, Nil), LeewayWindowConfig(None, None, None, None))
  val issuerConfig =
    IssuerConfig(Algorithm.none(), RegisteredConfig(None, None, Nil, false, false, None, None))

  val jwtVerifier = new JwtVerifier(verifierConfig)
  val jwtIssuer   = new JwtIssuer(issuerConfig)

  "CirceConversion" should {

    "convert circe (encoders & decoders) to claims (encoders & decoders)" in {
      val bar    = Bar("bar", 10)
      val jwt    = jwtIssuer.issueJwt(JwtClaims.ClaimsP(bar)).value
      val claims = jwtVerifier.verifyJwt[Bar](JwtToken.TokenP(jwt.token)).value

      claims.payload shouldBe bar
    }

    "convert circe (codec) to claims (encoders & decoders)" in {
      val foo    = Foo("foo", 10)
      val jwt    = jwtIssuer.issueJwt(JwtClaims.ClaimsP(foo)).value
      val claims = jwtVerifier.verifyJwt[Foo](JwtToken.TokenP(jwt.token)).value

      claims.payload shouldBe foo
    }

    "convert circe decoder to claims decoder and get error" in {
      val fooJson = """{"name":"Hello","age":"not number"}"""
      val jwt = JWT
        .create()
        .withPayload(Map(dataField -> fooJson).asJava)
        .sign(Algorithm.none())
        .pipe(NonEmptyString.unsafeFrom)
      val claims = jwtVerifier.verifyJwt[Foo](JwtToken.TokenP(jwt))

      claims.left.value shouldBe JwtVerifyError.DecodingError("Int: DownField(age)", null)
    }
  }
}
