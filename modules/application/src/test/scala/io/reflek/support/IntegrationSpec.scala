package io.reflek.support

import java.time.Clock

import scala.compiletime.uninitialized
import scala.concurrent.duration._
import scala.util.Random

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.actor.testkit.typed.scaladsl.LogCapturing
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.AskPattern._
import akka.cluster.typed.Cluster
import akka.cluster.typed.Join
import akka.cluster.typed.SelfUp
import akka.cluster.typed.Subscribe
import akka.persistence.query.PersistenceQuery
import akka.persistence.r2dbc.query.scaladsl.R2dbcReadJournal
import akka.stream.Materializer
import akka.util.Timeout

import com.dimafeng.testcontainers.ForAllTestContainer
import com.dimafeng.testcontainers.PostgreSQLContainer
import com.typesafe.config.Config
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.Location
import org.flywaydb.core.api.configuration.ClassicConfiguration
import org.scalatest.BeforeAndAfterAll
import org.scalatest.TestSuite
import org.scalatest.concurrent.Eventually
import org.scalatest.matchers.should.Matchers
import org.testcontainers.containers.{PostgreSQLContainer => JavaPostgreSQLContainer}
import org.testcontainers.utility.DockerImageName

import io.reflek.Application
import io.reflek.support.ConfigFactory.Environment
import io.reflek.support.IntegrationSpec._

/**
 * Mixin for setting up integration tests.
 * Add this trait to your Spec class and you'll get:
 *
 * - A Postgres database running in a testcontainer
 * - A single-node Akka Cluster
 *
 * By default, production config is loaded and used to configure the ActorSystem.
 * Any overrides can be provided by setting them in src/test/resources/integration-test.conf
 * Config loading is configured to use these overrides in favour of anything in production config.
 */
trait IntegrationSpec
    extends TestSuite
    with Matchers
    with Eventually
    with ForAllTestContainer
    with BeforeAndAfterAll
    with LogCapturing
    with FutureSupport {

  override val container: PostgreSQLContainer = PostgreSQLContainer(
    databaseName = DatabaseName,
    username = DatabaseUsername,
    password = DatabasePassword,
    dockerImageNameOverride = PostgresDockerImageName
  )

  var clock: Clock               = uninitialized // scalafix:ok
  var config: Config             = uninitialized // scalafix:ok
  var testKit: ActorTestKit      = uninitialized // scalafix:ok
  var cluster: Cluster           = uninitialized // scalafix:ok
  var system: ActorSystem[?]     = uninitialized // scalafix:ok
  var journal: R2dbcReadJournal  = uninitialized // scalafix:ok
  var materializer: Materializer = uninitialized // scalafix:ok
  var application: Application   = uninitialized // scalafix:ok

  override def afterStart(): Unit = {
    super.afterStart()

    val configuration = new ClassicConfiguration()
    configuration.setDataSource(container.jdbcUrl, container.username, container.password)
    configuration.setLocations(new Location("classpath:db/migration"))
    val flyway = new Flyway(configuration)
    val _      = flyway.migrate()

    clock = Clock.systemUTC()
    config = ConfigFactory.forIntegrationTesting(postgresProperties(container))
    testKit =
      ActorTestKit(s"IntegrationTest_${clock.instant().getEpochSecond}_${Random.alphanumeric.take(6).mkString}", config)
    system = testKit.system
    cluster = Cluster(system)
    journal = PersistenceQuery(system).readJournalFor[R2dbcReadJournal](R2dbcReadJournal.Identifier)
    materializer = Materializer(system)
    application = Application(using system)

    // Form a one-node cluster for testing purposes
    cluster.manager ! Join(cluster.selfMember.address)

    // After triggering the join we need to wait for the `SelfUp` message from the cluster
    // without waiting `selfMember.status` will be `REMOVED` rather than `UP`
    // we pass timeout and scheduler explicitly to avoid polluting the implicit context of the test spec
    val _ = FutureSupport.await(
      cluster.subscriptions
        .ask(Subscribe(_, classOf[SelfUp]))(Timeout(1.second), system.scheduler)
    )
  }
}

object IntegrationSpec {
  val PostgresDockerImageName: DockerImageName = DockerImageName.parse("postgres:13")
  val DatabaseName: String                     = "reflek"
  val DatabaseUsername: String                 = "reflek"
  val DatabasePassword: String                 = "reflek"

  def postgresProperties(container: PostgreSQLContainer): Environment = Map(
    "POSTGRES_HOST"     -> container.host,
    "POSTGRES_PORT"     -> container.mappedPort(JavaPostgreSQLContainer.POSTGRESQL_PORT).toString,
    "POSTGRES_USER"     -> DatabaseUsername,
    "POSTGRES_PASSWORD" -> DatabasePassword,
    "POSTGRES_DB"       -> DatabaseName
  )
}
