import com.lightbend.cinnamon.sbt.Cinnamon
import sbt.*
import sbt.Def.Classpath
import sbt.Keys.unmanagedJars

object Dependencies {

  object Versions {
    val scala = "3.5.1"

    // Akka
    val akkaActor            = "2.9.6"
    val akkaHttp             = "10.6.3"
    val akkaHttpSupport      = "10.5.3"
    val akkaPersistenceR2dbc = "1.2.5"
    val akkaProjection       = "1.5.7"
    val akkaManagement       = "1.5.3"

    // Typelevel
    val cats       = "2.12.0"
    val catsRetry  = "3.1.3"
    val enumeratum = "1.7.4"
    val circe      = "0.14.9"
    val protoCirce = "0.16.0"

    // Infrastructure
    val borer                  = "1.14.1"
    val janino                 = "3.1.12"
    val logback                = "1.5.8"
    val logstashLogbackEncoder = "8.0"
    val postgresDriver         = "42.7.4"
    val slick                  = "3.5.1"
    val slickPg                = "0.22.2"

    // Test
    val archUnit         = "1.3.0"
    val scalaTest        = "3.2.19"
    val scalaCheck       = "1.18.1"
    val scalaCheckEffect = "1.0.4"
    val scalaCheckPlus   = "3.2.18.0"

    // Munit
    val munit                 = "1.0.0"
    val scalacheckEffectMunit = "1.0.4"

    // Test Support
    val flywayCore          = "10.19.0"
    val flywayPostgres      = "10.19.0"
    val testcontainers      = "1.20.2"
    val testcontainersScala = "0.41.4"

    // Extism
    val extism = "1.0.1"
  }

  private lazy val akkaActor = Seq(
    "com.typesafe.akka" %% "akka-actor-typed"  % Versions.akkaActor,
    "com.typesafe.akka" %% "akka-stream-typed" % Versions.akkaActor,
    "com.typesafe.akka" %% "akka-slf4j"        % Versions.akkaActor
  )

  private lazy val akkaCluster: Seq[ModuleID] = Seq(
    "com.typesafe.akka" %% "akka-cluster-sharding-typed" % Versions.akkaActor,
    "com.typesafe.akka" %% "akka-cluster-tools"          % Versions.akkaActor,
    "com.typesafe.akka" %% "akka-cluster-typed"          % Versions.akkaActor,
    "com.typesafe.akka" %% "akka-discovery"              % Versions.akkaActor
  )

  private lazy val akkaHttp: Seq[ModuleID] = Seq(
    "com.typesafe.akka" %% "akka-http"            % Versions.akkaHttp,
    "com.typesafe.akka" %% "akka-http-spray-json" % Versions.akkaHttp,
    "com.typesafe.akka" %% "akka-http2-support"   % Versions.akkaHttpSupport
  )

  private lazy val akkaPersistence: Seq[ModuleID] = Seq(
    "com.typesafe.akka"  %% "akka-persistence-typed" % Versions.akkaActor,
    "com.typesafe.akka"  %% "akka-persistence-query" % Versions.akkaActor,
    "com.lightbend.akka" %% "akka-persistence-r2dbc" % Versions.akkaPersistenceR2dbc
  )

  private lazy val akkaProjection: Seq[ModuleID] = Seq(
    "com.lightbend.akka" %% "akka-projection-eventsourced" % Versions.akkaProjection,
    "com.lightbend.akka" %% "akka-projection-grpc"         % Versions.akkaProjection,
    "com.lightbend.akka" %% "akka-projection-r2dbc"        % Versions.akkaProjection
  )

  private lazy val akkaManagement: Seq[ModuleID] = Seq(
    "com.lightbend.akka.discovery"  %% "akka-discovery-kubernetes-api"     % Versions.akkaManagement,
    "com.lightbend.akka.management" %% "akka-management"                   % Versions.akkaManagement,
    "com.lightbend.akka.management" %% "akka-management-cluster-bootstrap" % Versions.akkaManagement
  )

  private lazy val cats: Seq[ModuleID] = Seq(
    "org.typelevel" %% "cats-core"   % Versions.cats,
    "org.typelevel" %% "cats-kernel" % Versions.cats
  )

  private lazy val catsUtils: Seq[ModuleID] = Seq(
    "com.github.cb372" %% "cats-retry"      % Versions.catsRetry,
    "com.github.cb372" %% "alleycats-retry" % Versions.catsRetry
  )

  private lazy val postgresDriver: Seq[ModuleID] = Seq(
    "org.postgresql" % "postgresql" % Versions.postgresDriver
  )

  private lazy val slick: Seq[ModuleID] = Seq(
    "com.typesafe.slick" %% "slick-hikaricp" % Versions.slick
  )

  private lazy val slickPg: Seq[ModuleID] = Seq(
    "com.github.tminglei" %% "slick-pg"            % Versions.slickPg,
    "com.github.tminglei" %% "slick-pg_circe-json" % Versions.slickPg
  )

  private val logging: Seq[ModuleID] = Seq(
    "ch.qos.logback"       % "logback-classic"          % Versions.logback,
    "net.logstash.logback" % "logstash-logback-encoder" % Versions.logstashLogbackEncoder,
    "org.codehaus.janino"  % "janino"                   % Versions.janino
  )

  private val scalaTest: Seq[ModuleID] = Seq(
    "org.scalatest"     %% "scalatest"         % Versions.scalaTest,
    "org.scalacheck"    %% "scalacheck"        % Versions.scalaCheck,
    "org.typelevel"     %% "scalacheck-effect" % Versions.scalaCheckEffect,
    "org.scalatestplus" %% "scalacheck-1-17"   % Versions.scalaCheckPlus
  )

  private val testcontainers: Seq[ModuleID] = Seq(
    "org.testcontainers" % "postgresql" % Versions.testcontainers
  )

  private val testcontainersScala: Seq[ModuleID] = Seq(
    "com.dimafeng" %% "testcontainers-scala"           % Versions.testcontainersScala,
    "com.dimafeng" %% "testcontainers-scala-scalatest" % Versions.testcontainersScala,
    "com.dimafeng" %% "testcontainers-scala-munit"     % Versions.testcontainersScala
  )

  private val flyway: Seq[ModuleID] = Seq(
    "org.flywaydb" % "flyway-core"                % Versions.flywayCore,
    "org.flywaydb" % "flyway-database-postgresql" % Versions.flywayPostgres
  )

  private val akkaTest: Seq[ModuleID] = Seq(
    "com.typesafe.akka" %% "akka-actor-testkit-typed" % Versions.akkaActor,
    "com.typesafe.akka" %% "akka-http-testkit"        % Versions.akkaHttp,
    "com.typesafe.akka" %% "akka-persistence-testkit" % Versions.akkaActor,
    "com.typesafe.akka" %% "akka-stream-testkit"      % Versions.akkaActor
  )

  lazy val archUnit: Seq[ModuleID] = Seq(
    "com.tngtech.archunit" % "archunit" % Versions.archUnit
  )

  private val akka: Seq[ModuleID] =
    akkaActor ++ akkaCluster ++ akkaHttp ++ akkaPersistence ++ akkaProjection ++ akkaManagement

  private val infrastructure: Seq[ModuleID] =
    akka ++ slick ++ slickPg ++ logging

  private val typelevel: Seq[ModuleID] =
    cats ++ catsUtils

  private val testing: Seq[ModuleID] =
    (scalaTest ++ testcontainers ++ testcontainersScala ++ akkaTest ++ archUnit ++ flyway).map(_ % Test)

  lazy val topLevelDependencies: Seq[ModuleID] =
    testing

  lazy val applicationDependencies: Seq[ModuleID] =
    infrastructure ++ typelevel ++ testing
}

