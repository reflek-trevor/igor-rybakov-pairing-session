import Dependencies.Versions
import com.typesafe.sbt.packager.docker.DockerPlugin.autoImport.{Docker, dockerBaseImage, dockerRepository, dockerUpdateLatest}
import org.typelevel.sbt.tpolecat.TpolecatPlugin.autoImport.tpolecatScalacOptions
import org.typelevel.scalacoptions.ScalacOptions
import sbt.{Def, *}
import sbt.Keys.*
import sbt.nio.Keys.{ReloadOnSourceChanges, onChangedBuildSource}

object Build {

  implicit class ProjectOps(self: Project) {
    def libraries(libs: Seq[ModuleID]): Project = {
      self.settings(libraryDependencies ++= libs)
    }
  }

  lazy val commonSettings: Seq[Def.Setting[?]] = {

    lazy val dockerSettings = Seq(
      dockerBaseImage    := "eclipse-temurin:21.0.3_9-jre",
      dockerUpdateLatest := true,
      dockerRepository := sys.env.get("DOCKER_REPOSITORY")
    )

    val projectSettings = Seq(
      scalaVersion          := Versions.scala,
      organization          := "io.reflek",
      semanticdbEnabled     := true,
      onChangedBuildSource  := ReloadOnSourceChanges,
      test / fork := false,
      run / fork            := true,
      run / javaOptions     ++= sys.props
        .get("config.resource")
        .fold(Seq.empty[String])(res => Seq(s"-Dconfig.resource=$res"))
    )

    val resolverSettings =
      resolvers ++= Seq(
        "Akka library repository" at "https://repo.akka.io/maven",
        "Akka library snapshot repository" at "https://repo.akka.io/snapshots"
      ) ++ Resolver.sonatypeOssRepos("snapshots")

    val compilationSettings = Seq(
      tpolecatScalacOptions ++= Set(
        ScalacOptions.explain
      )
    )

    val versioningSettings = Seq(
      version  ~= (_.replace('+', '_'))
    )

    dockerSettings ++ projectSettings ++ resolverSettings ++ compilationSettings ++ versioningSettings
  }

  lazy val doNotPublish: Seq[Def.Setting[?]] = Seq(
    publish / skip      := true,
    publishArtifact     := false,
    Docker / publish    := {},
    Docker / aggregate  := false
  )
}
