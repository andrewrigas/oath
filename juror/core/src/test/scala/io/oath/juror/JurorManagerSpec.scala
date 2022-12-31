package io.oath.juror

import io.oath.juror.JurorManager.JwtManager
import io.oath.jwt.model.JwtToken
import io.oath.jwt.testkit.AnyWordSpecBase

class JurorManagerSpec extends AnyWordSpecBase {

  "JurorManager" should {

    "create different token managers" in {
      val jurorManager = JurorManager.createOrFail(JurorToken)

      val accessTokenManager: JwtManager[JurorToken.AccessToken.type]   = jurorManager.as(JurorToken.AccessToken)
      val refreshTokenManager: JwtManager[JurorToken.RefreshToken.type] = jurorManager.as(JurorToken.RefreshToken)
      val activationEmailTokenManager: JwtManager[JurorToken.ActivationEmailToken.type] =
        jurorManager.as(JurorToken.ActivationEmailToken)
      val forgotPasswordTokenManager: JwtManager[JurorToken.ForgotPasswordToken.type] =
        jurorManager.as(JurorToken.ForgotPasswordToken)

      val accessToken          = accessTokenManager.issueJwt().value.token
      val refreshToken         = refreshTokenManager.issueJwt().value.token
      val activationEmailToken = activationEmailTokenManager.issueJwt().value.token
      val forgotPasswordToken  = forgotPasswordTokenManager.issueJwt().value.token

      accessTokenManager.verifyJwt(JwtToken.Token(accessToken)).isRight shouldBe true
      refreshTokenManager.verifyJwt(JwtToken.Token(refreshToken)).isRight shouldBe true
      activationEmailTokenManager.verifyJwt(JwtToken.Token(activationEmailToken)).isRight shouldBe true
      forgotPasswordTokenManager.verifyJwt(JwtToken.Token(forgotPasswordToken)).isRight shouldBe true
    }
  }

}
