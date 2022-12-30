package io.oath.juror

import io.oath.juror.JurorManager.Manager
import io.oath.juror.config.ConfigLoader
import io.oath.jwt.JwtManager

import scala.util.chaining.scalaUtilChainingOps

final class JurorManager[A] private (mapping: Map[A, JwtManager]) {

  def as[S <: A](tokenType: S): Manager[S] = mapping(tokenType)
}

object JurorManager {

  // Type information useful when issuing tokens to determine the token type.
  type Manager[_] = JwtManager

  def createOrFail[A <: TokenEnumEntry](tokenEnumEntry: TokenEnum[A]): JurorManager[A] =
    tokenEnumEntry.mapping.view
      .mapValues(configLocation => ConfigLoader.manager(configLocation))
      .mapValues(config => new JwtManager(config))
      .toMap
      .pipe(mapping => new JurorManager(mapping))

}
