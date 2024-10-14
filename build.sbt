import Build.*
import Dependencies.*

Global / scalaVersion := Dependencies.Versions.scala
Global / onChangedBuildSource := ReloadOnSourceChanges
Global / scalafmtOnCompile    := true

lazy val root = (project in file("."))
  .aggregate(application)
  .dependsOn(Array(application).map(_ % "test->test") *)
  .libraries(topLevelDependencies)
  .settings(commonSettings)
  .settings(doNotPublish)

lazy val application = (project in file("modules/application"))
  .libraries(applicationDependencies)
  .settings(commonSettings)
  .settings(
    scalafixOnCompile := true,
    semanticdbEnabled := true
  )
