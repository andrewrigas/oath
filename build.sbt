Global / onChangedBuildSource := ReloadOnSourceChanges

ThisBuild / scalaVersion := "2.13.10"
ThisBuild / organization := "io.github.andrewrigas"
ThisBuild / organizationName := "oath"
ThisBuild / organizationHomepage := Some(url("https://github.com/andrewrigas/oath"))
ThisBuild / scalafixDependencies += "com.github.liancheng" %% "organize-imports" % "0.6.0"
ThisBuild / coverageEnabled := true

ThisBuild / tlBaseVersion := "0.0"
ThisBuild / licenses := Seq(License.Apache2)
ThisBuild / developers := List(
  tlGitHubDev("andrewrigas", "Andreas Rigas")
)
ThisBuild / tlSonatypeUseLegacyHost := false
ThisBuild / startYear := Some(2022)

ThisBuild / githubWorkflowAddedJobs ++= Seq(
  WorkflowJob(
    id = "checklint",
    name = "Check code style",
    scalas = List(scalaVersion.value),
    steps = List(WorkflowStep.Checkout) ++ WorkflowStep.SetupJava(
      List(githubWorkflowJavaVersions.value.last)
    ) ++ githubWorkflowGeneratedCacheSteps.value ++ List(
      WorkflowStep.Sbt(
        List("checkLint"),
        name = Some("Check Scalafmt and Scalafix rules")
      )
    )
  )
)

lazy val root = Projects
  .createModule("oath", ".")
  .settings(publish / skip := true)
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
  .settings(publish / skip := true)

lazy val modules: Seq[ProjectReference] = Seq(
  jwtCore,
  jwtCirce,
  csrfCore
)
