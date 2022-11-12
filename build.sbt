ThisBuild / organization := "io.github.andrewrigas"
ThisBuild / organizationName := "oath"
ThisBuild / organizationHomepage := Some(url("https://github.com/andrewrigas/oath"))
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.13.10"
ThisBuild / scalafixDependencies += "com.github.liancheng" %% "organize-imports" % "0.6.0"
ThisBuild / coverageEnabled := true
Global / onChangedBuildSource := ReloadOnSourceChanges

lazy val root = Projects
  .createModule("oath", ".")
  .settings(Aliases.all)
  .aggregate(modules: _*)

lazy val jwtCore = Projects
  .createModule("jwt-core", "jwt/core")
  .settings(Dependencies.jwtCore)

lazy val jwtCirce = Projects
  .createModule("jwt-circe", "jwt/circe")
  .settings(Dependencies.jwtCirce)
  .dependsOn(jwtCore % "compile->compile;test->test")

lazy val csrfCore = Projects
  .createModule("csrf-core", "csrf/core")

lazy val modules: Seq[ProjectReference] = Seq(
  jwtCore,
  jwtCirce,
  csrfCore
)
