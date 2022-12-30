package io.oath.juror

import com.sun.tools.javac.util.StringUtils
import io.oath.juror.utils.FormatConversion
import enumeratum.Enum

import scala.tools.nsc.util.StringUtil

trait TokenEnum[A <: TokenEnumEntry] extends Enum[A] {

  private val objectNameRegex = """(?<=\$)(.*?)(?=\$)""".r

  private def convertToString(value: A): String = {
    val name = value.entryName
    objectNameRegex
      .findFirstIn(value.entryName)
      .getOrElse(name)
  }

  final def mapping: Map[A, String] =
    values
      .map(value =>
        value ->
          FormatConversion
            .convertUpperCamelToLowerHyphen(
              convertToString(value)
            ))
      .toMap
}
