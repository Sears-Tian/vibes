package com.vibes.actors

import akka.actor.{Actor, ActorRef, Props}
import akka.util.Timeout
import com.vibes.actions._
import com.vibes.models.VBlock

import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class NodeRepoActor(discoveryActor: ActorRef, reducerActor: ActorRef) extends Actor {
  implicit val timeout: Timeout                   = Timeout(20.seconds)
  private var registeredNodeActors: Set[ActorRef] = Set.empty

  override def preStart(): Unit = {
    println(s"NodeRegistryActor started ${self.path}")
  }

  override def receive: Receive = {
    case NodeRepoActions.RegisterNode =>
      println(s"REGISTER NODE....... ${registeredNodeActors.size}")
      val coordinates = NodeRepoActor.createCoordinatesOnLand()
      val actor = context.actorOf(
        NodeActor.props(context.parent, self, discoveryActor, reducerActor, coordinates._1, coordinates._2))
      registeredNodeActors += actor

    case NodeRepoActions.AnnounceStart(now) =>
      println("NodeRepoActions.AnnounceStart")
      registeredNodeActors.foreach(_ ! NodeActions.StartSimulation(now))

    case NodeRepoActions.AnnounceNextWorkRequestOnly =>
      registeredNodeActors.foreach(_ ! NodeActions.CastNextWorkRequestOnly)

    case NodeRepoActions.AnnounceNextWorkRequestAndMine(timestamp) =>
      registeredNodeActors.foreach(_ ! NodeActions.CastNextWorkRequestAndMine(timestamp, sender()))

    case NodeRepoActions.AnnounceEnd =>
      registeredNodeActors.foreach(_ ! NodeActions.End)
  }
}

object NodeRepoActor {
  private val coordinateLimits = List(
    // North America
    Coordinate(60, 68, -140, -120),
    Coordinate(51, 61, -127, -99),
    Coordinate(52, 57, -127, -95),
    Coordinate(45, 53, -118, -88),
    Coordinate(32, 43, -108, -88),
    Coordinate(44, 50, -76, -63),
    // South America
    Coordinate(-10, 2, -76, -53),
    Coordinate(-14, -4, -73, -42),
    Coordinate(-25, -14, -64, -50),
    Coordinate(-30, -25, -69, -53),
    // Africa
    Coordinate(17, 27, -9, 12),
    Coordinate(7, 17, -9, 12),
    Coordinate(17, 27, 12, 32),
    Coordinate(7, 17, 12, 32),
    Coordinate(-8, 1, 17, 36),
    Coordinate(-23, -14, 17, 32),
    // Europe
    Coordinate(46, 52, 10, 32),
    Coordinate(44, 48, 1, 10),
    Coordinate(60, 64, 25, 32),
    Coordinate(59, 62, 8, 15),
    Coordinate(44, 48, 1, 10),
    Coordinate(59, 62, 8, 15),
    // Australia
    Coordinate(-31, -19, 125, 144),
    Coordinate(-31, -19, 125, 144),
    Coordinate(-31, -19, 125, 144),
    // Asia
    Coordinate(42, 64, 53, 112),
    Coordinate(42, 64, 53, 112),
    Coordinate(42, 64, 53, 112),
    Coordinate(42, 64, 53, 112),
    Coordinate(58, 71, 85, 131),
    Coordinate(58, 71, 85, 131),
    Coordinate(58, 71, 85, 131),
    Coordinate(58, 71, 85, 131),
    Coordinate(61, 68, 142, 163),
    Coordinate(61, 68, 142, 163)
  )

  def randomBetween(start: Int, end: Int): Int = {
    start + scala.util.Random.nextInt(Math.abs(end - start) + 1)
  }

  def createCoordinatesOnLand(): (Double, Double) = {
    val coordinate = coordinateLimits(randomBetween(0, coordinateLimits.size - 1))
    (randomBetween(coordinate.latStart, coordinate.latEnd), randomBetween(coordinate.lngStart, coordinate.lngEnd))
  }

  def props(
    discoveryActor: ActorRef,
    reducerActor: ActorRef
  ): Props = Props(new NodeRepoActor(discoveryActor, reducerActor))
}

case class Coordinate(latStart: Int, latEnd: Int, lngStart: Int, lngEnd: Int)