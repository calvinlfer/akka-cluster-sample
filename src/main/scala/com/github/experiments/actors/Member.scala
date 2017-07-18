package com.github.experiments.actors

import akka.actor.SupervisorStrategy.Stop
import akka.actor.{Actor, ActorLogging, ReceiveTimeout}
import akka.cluster.sharding.ShardRegion
import akka.cluster.sharding.ShardRegion.Passivate
import com.github.experiments.SettingsExtension
import com.github.experiments.actors.Member._

object Member {
  sealed trait Command
  case object SayHello extends Command

  sealed trait Event
  case class HelloSaid(message: String) extends Event

  object Sharding {
    case class EntityEnvelope(id: String, command: Command)

    val shardName: String = "member-shard"

    val extractEntityId: ShardRegion.ExtractEntityId = {
      case EntityEnvelope(id, payload) => (id, payload)
    }

    def shardIdExtractor(numberOfShards: Int): ShardRegion.ExtractShardId = {
      case envelope: EntityEnvelope => (envelope.id.hashCode % numberOfShards).toString
    }
  }
}

class Member extends Actor with ActorLogging {
  val settings = SettingsExtension(context.system)

  override def preStart(): Unit = {
    log.info(s"Bringing up {}", self.path.name)
    context.setReceiveTimeout(settings.members.passivationTime)
  }

  override def receive: Receive = {
    case SayHello =>
      log.info("SayHello command received from {}", sender().path.name)
      sender() ! HelloSaid(s"hello ${sender().path.name}")

    case ReceiveTimeout =>
      context.parent ! Passivate(stopMessage = Stop)

    case Stop =>
      context stop self

    case e =>
      log.error("Unknown message {}", e)
  }
}
