package io.reflek.infra

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.ActorContext
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.typed.Cluster
import akka.cluster.typed.SelfUp
import akka.cluster.typed.Subscribe

private[infra] object WaitForClusterMembership {

  final case class Ready()

  sealed trait InfrastructureMessage
  final case class Start(replyTo: ActorRef[Ready]) extends InfrastructureMessage
  private final case class NodeMemberUp()          extends InfrastructureMessage

  private type Context = ActorContext[InfrastructureMessage]
  private type ReplyTo = ActorRef[Ready]

  def apply(): Behavior[Start] =
    Behaviors.setup[InfrastructureMessage](waitingForStartMessage).narrow[Start]

  private def waitingForStartMessage(context: Context): Behavior[InfrastructureMessage] =
    Behaviors.receiveMessagePartial[InfrastructureMessage] {
      case Start(replyTo) =>
        val cluster   = Cluster(context.system)
        val upAdapter = context.messageAdapter[SelfUp](_ => NodeMemberUp())
        cluster.subscriptions ! Subscribe(upAdapter, classOf[SelfUp])
        waitingForClusterMembership(replyTo)
    }

  private def waitingForClusterMembership(
      replyTo: ReplyTo
  ): Behavior[InfrastructureMessage] =
    Behaviors.receiveMessagePartial[InfrastructureMessage] {
      case _: NodeMemberUp =>
        replyTo ! Ready()
        Behaviors.ignore
    }
}
