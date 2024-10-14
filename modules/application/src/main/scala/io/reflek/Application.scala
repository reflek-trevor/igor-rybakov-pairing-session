package io.reflek

//import scala.concurrent.ExecutionContext
//import scala.concurrent.duration._

import akka.actor.typed.ActorSystem

import org.slf4j.LoggerFactory

case class Application()

object Application {

  private val log = LoggerFactory.getLogger(getClass)

  def apply(using system: ActorSystem[?]): Application = {
    log.info(s"${system.name} initialising...")

//    given ExecutionContext = system.executionContext
//    given ClusterSharding  = ClusterSharding(system)
//    given Timeout          = Timeout(2.seconds)

    new Application()
  }
}
