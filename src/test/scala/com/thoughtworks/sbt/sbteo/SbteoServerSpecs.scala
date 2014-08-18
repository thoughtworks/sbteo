package com.thoughtworks.sbt.sbteo

import java.util.UUID

import com.thoughtworks.sbt.sbteo.steps._
import com.twitter.finagle.ChannelWriteException
import com.twitter.finagle.websocket.WebSocket
import com.twitter.util.Await.result
import com.twitter.util.Duration.fromSeconds
import net.liftweb.json.JsonAST.{JNothing, JValue}
import net.liftweb.json.JsonDSL._
import net.liftweb.json.{compact, parse, render}
import org.specs2.mutable._


class SbteoServerSpecs extends Specification {
  sequential

  "A web socket server" should {
    "when not started" should {
      "has no server" in new GivenSocketServer {
        socketServer.server must beNone
      }
    }
    "when started" should {
      "has a server" in new GivenSocketServer {
        startedServer.server must beSome
      }
      "allows web socket connections" in new GivenSocketServer with GivenApiClient {
        startedServer
        val clientWs: WebSocket = result(futureClient(), fromSeconds(1))
        clientWs.close()
      }

      "responds to ping" in new GivenSocketServer with GivenApiClient {
        startedServer
        val requestId = UUID.randomUUID().toString

        private val payload: String = result(futureClient() flatMap { ws =>
          broker !!
            compact(render(
              ("requestId" -> requestId) ~
                ("type" -> "ping")))

          ws.messages.?
        }, fromSeconds(1))

        var json = parse(payload)
        (json \ "requestId" values) must beEqualTo(requestId)

        (json \ "responseId" values) must not beNull

        json \ "error" must beEqualTo(JNothing)
      }

      "responds to autocomplete" in new GivenSocketServer with GivenApiClient {
        startedServer
        val requestId:String = UUID.randomUUID().toString
        val payload = result(futureClient() flatMap { ws =>
          broker !!
            compact(render(
              ("requestId" -> requestId) ~
                ("type" -> "auto-complete") ~
                ("doc" -> List("")) ~
                ("position" ->
                  ("row" -> 1) ~
                    ("column" -> 1))))
          ws.messages.?
        }, fromSeconds(10))

        val json = parse(payload)

        json \ "error" must beEqualTo(JNothing)

        (json \ "type" values) must beEqualTo("auto-complete")

        private val completions: List[JValue] = (json \ "completions").children

        completions must beEmpty

      }

      "autocompletes basic scala code" in new GivenSocketServer with GivenApiClient with GivenBasicSource {
        startedServer

        val requestId:String = UUID.randomUUID().toString
        val payload = result(futureClient() flatMap { ws =>
          val docValue: List[String] = sourceDocument.split("\n").toList
          broker !!
            compact(render(
              ("requestId" -> requestId) ~
                ("type" -> "auto-complete") ~
                ("doc" -> docValue) ~
                ("position" ->
                  ("row" -> 1) ~
                    ("column" -> 1))))
          ws.messages.?
        }, fromSeconds(10))

        val json = parse(payload)

        json \ "error" must beEqualTo(JNothing)

        (json \ "type" values) must beEqualTo("auto-complete")

        private val completions: List[JValue] = (json \ "completions").children

        completions must not beEmpty

        ((completions(0) \ "symbol" values) must not).beNull

        (completions(0) \ "priority" values) must beEqualTo(1)

      }

      "when stopped" should {
        "not accept socket connections" in new GivenSocketServer with GivenApiClient {
          startedServer.stop()

          def attemptConnect(): Unit = {
            val webSocket: WebSocket = result(futureClient(), fromSeconds(10))
          }

          attemptConnect must throwA[ChannelWriteException]
        }
      }
    }
  }
}