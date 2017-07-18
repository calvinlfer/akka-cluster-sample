package com.github.experiments

import akka.actor.{ExtendedActorSystem, Extension, ExtensionId, ExtensionIdProvider}
import com.typesafe.config.Config

import scala.concurrent.duration.{Duration, FiniteDuration}

object SettingsExtension extends ExtensionId[Settings] with ExtensionIdProvider {
  override def createExtension(system: ExtendedActorSystem): Settings = new Settings(system)
  override def lookup() = SettingsExtension
}

class Settings(config: Config) extends Extension {
  def this(system: ExtendedActorSystem) = this(system.settings.config)

  object members {
    val numberOfShards: Int = config.getInt("members.number-of-shards")

    val passivationTime: FiniteDuration = {
      val duration = Duration(config.getString("members.passivation-time"))
      FiniteDuration(duration.length, duration.unit)
    }
  }
}
