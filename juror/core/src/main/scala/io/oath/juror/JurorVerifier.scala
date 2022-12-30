package io.oath.juror

import io.oath.juror.JurorVerifier.Verifier
import io.oath.juror.config.ConfigLoader
import io.oath.jwt.JwtVerifier

import scala.util.chaining.scalaUtilChainingOps

final class JurorVerifier[A] private (mapping: Map[A, JwtVerifier]) {

  def as[S <: A](tokenType: S): Verifier[S] = mapping(tokenType)
}

object JurorVerifier {

  // Type information useful when issuing tokens to determine the token type.
  type Verifier[_] = JwtVerifier

  def createOrFail[A <: TokenEnumEntry](tokenEnumEntry: TokenEnum[A]): JurorVerifier[A] =
    tokenEnumEntry.mapping.view
      .mapValues(configLocation => ConfigLoader.verifier(configLocation))
      .mapValues(config => new JwtVerifier(config))
      .toMap
      .pipe(mapping => new JurorVerifier(mapping))

}
