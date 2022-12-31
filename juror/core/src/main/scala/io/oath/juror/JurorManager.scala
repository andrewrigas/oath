package io.oath.juror

import io.oath.juror.JurorManager.JwtManager
import io.oath.juror.config.ConfigLoader
import io.oath.jwt.{JwtManager => JManager}

import scala.util.chaining.scalaUtilChainingOps

final class JurorManager[A] private (mapping: Map[A, JManager]) {

  def as[S <: A](tokenType: S): JwtManager[S] = mapping(tokenType)
}

object JurorManager {

  // Type alias with extra information, useful to determine the token type.
  type JwtManager[_] = JManager

  def createOrFail[A <: TokenEnumEntry](tokenEnumEntry: TokenEnum[A]): JurorManager[A] =
    tokenEnumEntry.mapping.view
      .mapValues(configLocation => ConfigLoader.manager(configLocation))
      .mapValues(config => new JManager(config))
      .toMap
      .pipe(mapping => new JurorManager(mapping))

}
