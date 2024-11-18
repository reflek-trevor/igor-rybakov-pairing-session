package io.reflek.reserve

import akka.actor.typed.ActorRef
import akka.pattern.StatusReply
import io.reflek.infra.AkkaSerializable

import java.time.{Instant, LocalDateTime}
import java.util.UUID

object Reservation {

  sealed trait Command extends AkkaSerializable
  // we gonna pull the information about src and dest by flightId to be consistent with our partners on request time.
  // However we gonna use explicitly src and dest and flightId FlightReserved event because it's a log we want to trace history
  final case class ReserveFlight(reserveId: UUID, flightId: String, customerId: String, replyTo: ActorRef[StatusReply[Summary]])

  final case class CancelReservation(reserveId: UUID, replyTo: ActorRef[StatusReply[Summary]])

  final case class CheckoutReservation(reserveId: UUID, replyTo: ActorRef[StatusReply[Summary]])

  final case class ChangeReservation(reserveId: UUID, flightId: String, replyTo: ActorRef[StatusReply[Summary]])



  sealed trait Event extends AkkaSerializable

  final case class FlightReserved(reserveId: UUID, src: String, dest: String, departure: LocalDateTime, arrival: LocalDateTime, flightId: String, customerId: String)

  final case class ReservationChanged(reserveId: UUID, src: String, dest: String, departure: LocalDateTime, arrival: LocalDateTime, flightId: String, customerId: String)

  final case class ReservationCanceled(reserveId: UUID,  src: String, dest: String, departure: LocalDateTime, arrival: LocalDateTime, flightId: String, customerId: String)

  // Reservation summary that will returned each time Reservation requested by Id
  final case class FlightInfo( flightId: String, src: String, dest: String, departure: LocalDateTime, arrival: LocalDateTime)

  final case class Summary(reserveId: UUID, flight: FlightInfo, customerId: String, canceled: Boolean, checkoutDate: Option[Instant])

  //checkoutDate will indicate we have checkout reservation for payment
  final case class State(reserveId: UUID, flight: FlightInfo, customerId: String, canceled: Boolean = false, checkoutDate: Option[Instant]) extends AkkaSerializable


}
