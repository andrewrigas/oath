package io.oath.jwt

import java.time.Instant

import com.auth0.jwt.interfaces.DecodedJWT
import eu.timepit.refined.types.string.NonEmptyString

import scala.jdk.CollectionConverters.CollectionHasAsScala

package object utils {

  private[oath] implicit class DecodedJWTOps(private val decodedJWT: DecodedJWT) {
    def getOptionNonEmptyStringIssuer: Option[NonEmptyString] =
      Option(decodedJWT.getIssuer)
        .flatMap(NonEmptyString.unapply)

    def getOptionNonEmptyStringSubject: Option[NonEmptyString] =
      Option(decodedJWT.getSubject)
        .flatMap(NonEmptyString.unapply)

    def getOptionNonEmptyStringID: Option[NonEmptyString] =
      Option(decodedJWT.getId)
        .flatMap(NonEmptyString.unapply)

    def getSeqNonEmptyStringAudience: Seq[NonEmptyString] =
      Option(decodedJWT.getAudience).map(_.asScala).toSeq.flatMap(_.flatMap(NonEmptyString.unapply))

    def getOptionExpiresAt: Option[Instant] =
      Option(decodedJWT.getExpiresAt).map(_.toInstant)

    def getOptionIssueAt: Option[Instant] =
      Option(decodedJWT.getIssuedAt).map(_.toInstant)

    def getOptionNotBefore: Option[Instant] =
      Option(decodedJWT.getNotBefore).map(_.toInstant)
  }

}
