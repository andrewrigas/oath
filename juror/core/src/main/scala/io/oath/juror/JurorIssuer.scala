package io.oath.juror

import io.oath.juror.JurorIssuer.Issuer
import io.oath.juror.config.ConfigLoader
import io.oath.jwt.JwtIssuer

import scala.util.chaining.scalaUtilChainingOps

final class JurorIssuer[A] private (mapping: Map[A, JwtIssuer]) {

  def as[S <: A](tokenType: S): Issuer[S] = mapping(tokenType)
}

object JurorIssuer {

  // Type information useful when issuing tokens to determine the token type.
  type Issuer[_] = JwtIssuer

  def createOrFail[A <: TokenEnumEntry](tokenEnumEntry: TokenEnum[A]): JurorIssuer[A] =
    tokenEnumEntry.mapping.view
      .mapValues(configLocation => ConfigLoader.issuer(configLocation))
      .mapValues(config => new JwtIssuer(config))
      .toMap
      .pipe(mapping => new JurorIssuer(mapping))

}
