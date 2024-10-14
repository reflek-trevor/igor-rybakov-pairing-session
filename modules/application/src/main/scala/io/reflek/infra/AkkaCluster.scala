package io.reflek.infra

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.AskPattern._
import akka.util.Timeout

import com.typesafe.config.Config

import io.reflek.infra.WaitForClusterMembership.Start

final class AkkaCluster private (private val system: ActorSystem[Start]) {

  def startup(timeout: Timeout): Future[ActorSystem[?]] =
    system.ask(replyTo => Start(replyTo))(timeout, system.scheduler).map(_ => system)(system.executionContext)

  def shutdown(): Future[Unit] = {
    system.terminate()
    system.whenTerminated.map(_ => ())(ExecutionContext.global)
  }
}

object AkkaCluster {

  def apply(name: String, config: Config): AkkaCluster = {
    val system = ActorSystem[Start](WaitForClusterMembership(), name, config)
    new AkkaCluster(system)
  }
}
