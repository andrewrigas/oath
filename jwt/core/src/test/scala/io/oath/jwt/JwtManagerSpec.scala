package io.oath.jwt

import io.oath.jwt.config.ManagerConfig
import io.oath.jwt.model.JwtToken
import io.oath.jwt.testkit.{AnyWordSpecBase, PropertyBasedTesting}

class JwtManagerSpec extends AnyWordSpecBase with PropertyBasedTesting {

  "JwtManager" should {

    "be able to issue and verify jwt tokens" in forAll { config: ManagerConfig =>
      val jwtManager = new JwtManager(config)

      val jwt = jwtManager.issueJwt().value
      jwtManager.verifyJwt(JwtToken.Token(jwt.token)).value.registered shouldBe jwt.claims.registered
    }
  }
}
