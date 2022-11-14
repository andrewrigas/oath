package io.oath.jwt.model

import eu.timepit.refined.types.string.NonEmptyString

sealed trait JwtToken {
  def token: NonEmptyString
}

object JwtToken {

  final case class Token(token: NonEmptyString) extends JwtToken

  final case class TokenH(token: NonEmptyString) extends JwtToken

  final case class TokenP(token: NonEmptyString) extends JwtToken

  final case class TokenHP(token: NonEmptyString) extends JwtToken
}
