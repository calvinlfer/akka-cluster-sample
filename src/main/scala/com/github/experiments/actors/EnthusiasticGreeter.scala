package com.github.experiments.actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.github.experiments.actors.Member.{HelloSaid, SayHello}
import com.github.experiments.actors.Member.Sharding.EntityEnvelope

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

object EnthusiasticGreeter {
  def props(memberRegion: ActorRef): Props = Props(new EnthusiasticGreeter(memberRegion))
}

class EnthusiasticGreeter(memberRegion: ActorRef) extends Actor with ActorLogging {
  override def preStart(): Unit = {
    implicit val ec: ExecutionContext = context.dispatcher
    val random = new scala.util.Random()
    context.system.scheduler.schedule(
      initialDelay = 2.seconds,
      interval = 10.seconds,
      receiver = memberRegion,
      message = EntityEnvelope(random.nextString(5), SayHello)
    )
  }

  override def receive: Receive = {
    case HelloSaid(message) => log.info(message)
  }
}
