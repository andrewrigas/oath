import sbt.Keys.libraryDependencies
import sbt._

object Dependencies {

  object Versions {
    val scalaTest          = "3.2.14"
    val scalaTestPlusCheck = "3.2.11.0"
    val scalacheck         = "1.17.0"
    val javaJWT            = "4.2.1"
    val config             = "1.4.2"
    val cats               = "2.9.0"
    val bcprov             = "1.72"
    val jackson            = "2.14.1"
    val logbackClassic     = "1.4.5"
    val scalaLogging       = "3.9.5"
    val refined            = "0.10.1"
    val circe              = "0.14.3"
    val jsoniterScala      = "2.18.0"
  }

  object Testing {
    val scalaTest          = "org.scalatest"     %% "scalatest"       % Versions.scalaTest          % Test
    val scalaTestPlusCheck = "org.scalatestplus" %% "scalacheck-1-15" % Versions.scalaTestPlusCheck % Test
    val scalacheck         = "org.scalacheck"    %% "scalacheck"      % Versions.scalacheck         % Test

    val all = Seq(scalaTest, scalaTestPlusCheck, scalacheck)
  }

  object Circe {
    val core    = "io.circe" %% "circe-core"    % Versions.circe
    val generic = "io.circe" %% "circe-generic" % Versions.circe
    val parser  = "io.circe" %% "circe-parser"  % Versions.circe

    val all = Seq(core, generic, parser)
  }

  object JsoniterScala {
    val core = "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-core" % Versions.jsoniterScala
    val macros =
      "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-macros" % Versions.jsoniterScala % "provided"

    val all = Seq(core, macros)
  }

  object Refined {
    val core       = "eu.timepit" %% "refined"            % Versions.refined
    val scalacheck = "eu.timepit" %% "refined-scalacheck" % Versions.refined % Test

    val all = Seq(core, scalacheck)
  }

  object Utils {
    val config  = "com.typesafe"               % "config"           % Versions.config
    val cats    = "org.typelevel"             %% "cats-core"        % Versions.cats
    val bcprov  = "org.bouncycastle"           % "bcprov-jdk18on"   % Versions.bcprov
    val jackson = "com.fasterxml.jackson.core" % "jackson-databind" % Versions.jackson

    val jwt  = Seq(config, cats, bcprov, jackson)
    val csrf = Seq(config, cats)
  }

  object Auth0 {
    val javaJWT = "com.auth0" % "java-jwt" % Versions.javaJWT

    val all = Seq(javaJWT)
  }

  lazy val jwtCore =
    libraryDependencies ++= Testing.all ++ Refined.all ++ Auth0.all ++ Utils.jwt ++ Circe.all.map(_ % Test)

  lazy val jwtCirce =
    libraryDependencies ++= Circe.all

  lazy val jwtJsoniterScala =
    libraryDependencies ++= JsoniterScala.all

  lazy val csrfCore =
    libraryDependencies ++= Refined.all ++ Utils.csrf ++ Testing.all
}
