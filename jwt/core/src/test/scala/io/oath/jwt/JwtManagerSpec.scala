package io.oath.jwt

import io.oath.jwt.config.JwtManagerConfig
import io.oath.jwt.model.JwtToken
import io.oath.jwt.syntax._
import io.oath.jwt.testkit.{AnyWordSpecBase, PropertyBasedTesting}

class JwtManagerSpec extends AnyWordSpecBase with PropertyBasedTesting {

  "JwtManager" should {

    "be able to issue and verify jwt tokens without claims" in forAll { config: JwtManagerConfig =>
      val jwtManager = new JwtManager(config)

      val jwt = jwtManager.issueJwt().value
      jwtManager.verifyJwt(JwtToken.Token(jwt.token)).value.registered shouldBe jwt.claims.registered
    }

    "be able to issue and verify jwt tokens with header claims" in forAll {
      (config: JwtManagerConfig, nestedHeader: NestedHeader) =>
        val jwtManager = new JwtManager(config)

        val claims = nestedHeader.toClaimsH
        val jwt    = jwtManager.issueJwt(claims).value
        jwtManager.verifyJwt[NestedHeader](JwtToken.TokenH(jwt.token)).value shouldBe claims
          .copy(registered = jwt.claims.registered)
    }

    "be able to issue and verify jwt tokens with payload claims" in forAll {
      (config: JwtManagerConfig, nestedPayload: NestedPayload) =>
        val jwtManager = new JwtManager(config)

        val claims = nestedPayload.toClaimsP
        val jwt    = jwtManager.issueJwt(claims).value
        jwtManager.verifyJwt[NestedPayload](JwtToken.TokenP(jwt.token)).value shouldBe claims
          .copy(registered = jwt.claims.registered)
    }

    "be able to issue and verify jwt tokens with header & payload claims" in forAll {
      (config: JwtManagerConfig, nestedHeader: NestedHeader, nestedPayload: NestedPayload) =>
        val jwtManager = new JwtManager(config)

        val claims = (nestedHeader, nestedPayload).toClaimsHP
        val jwt    = jwtManager.issueJwt(claims).value
        jwtManager.verifyJwt[NestedHeader, NestedPayload](JwtToken.TokenHP(jwt.token)).value shouldBe claims
          .copy(registered = jwt.claims.registered)
    }
  }
}
