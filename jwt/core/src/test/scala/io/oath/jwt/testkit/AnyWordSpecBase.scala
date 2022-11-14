package io.oath.jwt.testkit

import org.scalatest.concurrent.{Eventually, ScalaFutures}
import org.scalatest.matchers.should
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.{EitherValues, LoneElement, OptionValues}

import scala.concurrent.duration.DurationInt

class AnyWordSpecBase
    extends AnyWordSpec
    with should.Matchers
    with OptionValues
    with EitherValues
    with Eventually
    with ScalaFutures
    with LoneElement {
  override implicit val patienceConfig: PatienceConfig = PatienceConfig(2.seconds, 200.millis)
}
