package com.github.experiments

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings}
import com.github.experiments.actors.{EnthusiasticGreeter, Member}

object Main extends App {
  val system = ActorSystem("akka-cluster-sample")
  val settings = SettingsExtension(system)

  // Aggregate
  val memberShardRegion: ActorRef = ClusterSharding(system).start(
    typeName = Member.Sharding.shardName,
    entityProps = Props[Member],
    settings = ClusterShardingSettings(system),
    extractEntityId = Member.Sharding.extractEntityId,
    extractShardId = Member.Sharding.shardIdExtractor(settings.members.numberOfShards)
  )

  val greeter: ActorRef = system.actorOf(EnthusiasticGreeter.props(memberShardRegion), "greeter")
}
