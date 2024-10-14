package io.reflek

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.Failure
import scala.util.Success

import akka.actor.typed.ActorSystem
import akka.util.Timeout

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

import io.reflek.infra.AkkaCluster

object Main {

  private val log = LoggerFactory.getLogger(getClass)

  private val AkkaSystemName = "quantum-asset"

  def main(args: Array[String]): Unit = {
    log.info(s"$AkkaSystemName cluster node starting up...")

    init(ConfigFactory.load()).onComplete {
      case Success(system) => Application(using system)
      case Failure(cause)  => log.error("Terminating due to initialization failure.", cause)
    }
  }

  private def init(config: Config): Future[ActorSystem[?]] =
    AkkaCluster(name = AkkaSystemName, config).startup(Timeout(30.seconds))
}
