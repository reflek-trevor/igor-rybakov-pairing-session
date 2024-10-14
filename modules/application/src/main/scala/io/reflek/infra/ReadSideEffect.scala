package io.reflek.infra

import scala.concurrent.Future

import akka.Done
import akka.NotUsed
import akka.actor.typed.ActorSystem
import akka.cluster.sharding.typed.ShardedDaemonProcessSettings
import akka.cluster.sharding.typed.scaladsl.ShardedDaemonProcess
import akka.persistence.query.Offset
import akka.persistence.query.typed.EventEnvelope
import akka.persistence.r2dbc.query.scaladsl.R2dbcReadJournal
import akka.projection.Projection
import akka.projection.ProjectionBehavior
import akka.projection.ProjectionContext
import akka.projection.ProjectionId
import akka.projection.eventsourced.scaladsl.EventSourcedProvider
import akka.projection.r2dbc.scaladsl.R2dbcProjection
import akka.projection.scaladsl.SourceProvider
import akka.stream.scaladsl.FlowWithContext

trait EventHandler[E] extends (EventEnvelope[E] => Future[Done])

object ReadSideEffect {

  case class Config(name: String, numberOfInstances: Int, entityType: String)

  def init[E](eventHandler: EventHandler[E])(using ActorSystem[?], Config): Unit = {
    val flow = FlowWithContext[EventEnvelope[E], ProjectionContext].mapAsync(1)(eventHandler)
    init(flow)
  }

  def init[E](
      flow: FlowWithContext[EventEnvelope[E], ProjectionContext, Done, ProjectionContext, NotUsed]
  )(using system: ActorSystem[?], config: Config): Unit = {

    def sourceProvider(sliceRange: Range): SourceProvider[Offset, EventEnvelope[E]] =
      EventSourcedProvider
        .eventsBySlices[E](
          system,
          readJournalPluginId = R2dbcReadJournal.Identifier,
          config.entityType,
          sliceRange.min,
          sliceRange.max
        )

    def projection(name: String, sliceRange: Range): Projection[EventEnvelope[E]] = {
      val minSlice     = sliceRange.min
      val maxSlice     = sliceRange.max
      val projectionId = ProjectionId(config.name, s"${name}-$minSlice-$maxSlice")

      R2dbcProjection
        .atLeastOnceFlow(projectionId, settings = None, sourceProvider(sliceRange), handler = flow)(system)
    }

    val _ = ShardedDaemonProcess(system).initWithContext(
      name = config.name,
      initialNumberOfInstances = config.numberOfInstances,
      behaviorFactory = {
        daemonContext =>
          val sliceRanges =
            EventSourcedProvider.sliceRanges(system, R2dbcReadJournal.Identifier, daemonContext.totalProcesses)
          val sliceRange = sliceRanges(daemonContext.processNumber)
          ProjectionBehavior(projection(config.name, sliceRange))
      },
      ShardedDaemonProcessSettings(system),
      stopMessage = ProjectionBehavior.Stop
    )
  }
}
