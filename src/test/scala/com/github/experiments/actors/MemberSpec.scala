package com.github.experiments.actors

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import com.github.experiments.actors.Member.{HelloSaid, SayHello}
import com.typesafe.config.ConfigFactory
import org.scalatest.{BeforeAndAfterAll, FunSpecLike, MustMatchers}

/**
  * Blackbox test of a Member Actor
  */
class MemberSpec extends TestKit(ActorSystem("member-test-system", ConfigFactory.load("test")))
  with FunSpecLike with MustMatchers with ImplicitSender with BeforeAndAfterAll {

  describe("Member Actor specification") {
    it("responds with hello said when you say hello") {
      val member = system.actorOf(Props[Member], "member")
      member ! SayHello
      expectMsgPF() {
        case HelloSaid(msg) => msg must startWith("hello")
      }
    }
  }

  override def afterAll(): Unit = system.terminate()
}
