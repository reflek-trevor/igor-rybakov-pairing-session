package io.reflek.infra

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

import akka.actor.typed.ActorRef
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.cluster.sharding.typed.scaladsl.EntityRef
import akka.cluster.sharding.typed.scaladsl.EntityTypeKey
import akka.util.Timeout

object Ask {
  type PartialReplyHandler[Reply, Result] = PartialFunction[Reply, Result]

  final case class UnhandledReplyError[Reply](reply: Reply)
      extends RuntimeException(s"Unhandled reply [${reply.toString}]")

  def ask[Command, Reply, Error, Result](
      entityTypeKey: EntityTypeKey[Command],
      entityId: String,
      commandFactory: ActorRef[Reply] => Command
  )(
      partialReplyHandler: PartialReplyHandler[Reply, Result]
  )(using sharding: ClusterSharding, ec: ExecutionContext, timeout: Timeout): Future[Result] =
    entityRefFor(entityTypeKey, entityId)
      .ask[Reply](commandFactory(_))
      .flatMap(
        reply =>
          partialReplyHandler
            .lift(reply)
            .map(result => Future.successful(result))
            .getOrElse(
              Future.failed(UnhandledReplyError(reply))
            )
      )

  def tell[Command](entityTypeKey: EntityTypeKey[Command], entityId: String, command: Command)(using
      sharding: ClusterSharding
  ): Unit =
    entityRefFor(entityTypeKey, entityId).tell(command)

  private def entityRefFor[Command](entityTypeKey: EntityTypeKey[Command], entityId: String)(using
      sharding: ClusterSharding
  ): EntityRef[Command] =
    sharding.entityRefFor(entityTypeKey, entityId)
}
