package io.oath.jwt.circe

import io.circe.{parser, Decoder}
import io.oath.jwt.ClaimsDecoder
import io.oath.jwt.model.JwtVerifyError

trait CirceDecoderConversion {

  implicit def circeDecoderConversion[A](implicit decoder: Decoder[A]): ClaimsDecoder[A] = json =>
    parser
      .parse(json)
      .left
      .map(parsingFailure => JwtVerifyError.DecodingError(parsingFailure.message, parsingFailure.underlying))
      .flatMap(_.as[A].left.map(decodingFailure =>
        JwtVerifyError.DecodingError(decodingFailure.getMessage(), decodingFailure.getCause)))

}
