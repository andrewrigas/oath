package io.oath.juror

import io.oath.juror.JurorIssuer.Issuer
import io.oath.jwt.model.{IssueJwtError, Jwt, JwtClaims}

object Main extends App {

  sealed trait Token extends TokenEnumEntry

  object Token extends TokenEnum[Token] {
    case object AccessToken extends Token
    case object RefreshToken extends Token
    case object ActivationEmailToken extends Token
    case object ForgotPasswordToken extends Token

    override def values: IndexedSeq[Token] = findValues
  }

  val x: JurorIssuer[Token] = JurorIssuer.createOrFail(Token)
  val s: Issuer[Token.AccessToken.type] = x.as(Token.AccessToken)
  val b: Either[IssueJwtError, Jwt[JwtClaims.Claims]] = s.issueJwt()
  println(b.getOrElse(throw new IllegalArgumentException("sd")).token)
}
