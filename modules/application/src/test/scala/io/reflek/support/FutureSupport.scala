package io.reflek.support

import scala.concurrent.Future

import cats.data.EitherT
import org.scalatest.concurrent.PatienceConfiguration.Interval
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.Millis
import org.scalatest.time.Seconds
import org.scalatest.time.Span

trait FutureSupport extends ScalaFutures {

  protected def awaitTimeout: Timeout = Timeout(Span(10, Seconds))

  protected def awaitInterval: Interval = Interval(Span(10, Millis))

  def await[T](f: Future[T]): T =
    whenReady(f, awaitTimeout, awaitInterval)(identity)

  def awaitRight[E, T](e: Future[Either[E, T]]): T =
    whenReady(e, awaitTimeout, awaitInterval)(expectRight)

  def awaitLeft[E, T](e: Future[Either[E, T]]): E =
    whenReady(e, awaitTimeout, awaitInterval)(expectLeft)

  def awaitRightT[E, T](e: EitherT[Future, E, T]): T =
    whenReady(e.value, awaitTimeout, awaitInterval)(expectRight)

  def awaitLeftT[E, T](e: EitherT[Future, E, T]): E =
    whenReady(e.value, awaitTimeout, awaitInterval)(expectLeft)

  private def expectRight[E, T]: Either[E, T] => T = {
    case Left(error)   => throw new IllegalStateException(s"Expected Either.right, got Either.left [$error]")
    case Right(result) => result
  }

  private def expectLeft[E, T]: Either[E, T] => E = {
    case Left(error)   => error
    case Right(result) => throw new IllegalStateException(s"Expected Either.left, got Either.right [$result]")
  }
}

object FutureSupport extends FutureSupport
