package com.thoughtworks.sbt.sbteo

import java.util.UUID

import com.thoughtworks.sbt.sbteo.steps._
import com.twitter.concurrent.Broker
import com.twitter.finagle.ChannelWriteException
import com.twitter.finagle.websocket.WebSocket
import com.twitter.util.Await.result
import com.twitter.util.Duration.fromSeconds
import com.twitter.util.Future
import net.liftweb.json.JsonAST.{JNothing, JValue}
import net.liftweb.json.JsonDSL._
import net.liftweb.json.{compact, parse, render}
import org.specs2.mutable._

class SbteoServerSpecs extends Specification {
  sequential

  def sendPing(broker: Broker[String], id: String): (WebSocket) => Future[String] = {
    ws =>
      broker !!
        compact(render(
          ("requestId" -> id) ~
            ("type" -> "ping")))

      ws.messages.?
  }

  def sendAutoComplete(broker: Broker[String], requestId: String, row: Int, column: Int = 1, document: String): (WebSocket) => Future[String] = { ws =>
    val docValue: List[String] = document.split("\n").toList
    broker !!
      compact(render(
        ("requestId" -> requestId) ~
          ("type" -> "auto-complete") ~
          ("doc" -> docValue) ~
          ("position" ->
            ("row" -> row) ~
              ("column" -> column))))
    ws.messages.?
  }


  "A web socket server" should {
    "when not started" should {
      "has no server" in new GivenSocketServer {
        socketServer.server must beNone
      }
    }
    "when configured using environment vars" should {
      "when started" should {
        "respond to ping on different socket" in new GivenSocketServer with GivenApiClient with GivenRuntimeConfiguration {
          givenSystemProperty("sbteo.endpoint", "localhost:8889")
          givenStartedServer
          val jv = result(futureClient(port = 8889)
            flatMap sendPing(broker, UUID.randomUUID().toString)
            map { s => parse(s)}, fromSeconds(1))

          jv \ "error" must beEqualTo(JNothing)
        }
        "not responsd to ping when host is blank" in new GivenSocketServer with GivenApiClient with GivenRuntimeConfiguration {
          givenSystemProperty("sbteo.endpoint", "")
          givenStartedServer
          val jv = result(futureClient(port = 8888) flatMap sendPing(broker, UUID.randomUUID().toString) map { s => parse(s)}, fromSeconds(1))
          jv \ "error" must beEqualTo(JNothing)
        }
      }
    }
    "when started" should {
      "has a server" in new GivenSocketServer {
        givenStartedServer.server must beSome
      }
      "allows web socket connections" in new GivenSocketServer with GivenApiClient {
        givenStartedServer
        val clientWs: WebSocket = result(futureClient(), fromSeconds(1))
      }

      "responds to ping" in new GivenSocketServer with GivenApiClient {
        givenStartedServer
        val requestId = UUID.randomUUID().toString

        private val payload: String = result(futureClient() flatMap sendPing(broker, requestId), fromSeconds(1))

        var json = parse(payload)
        (json \ "requestId" values) must beEqualTo(requestId)

        (json \ "responseId" values) must not beNull

        json \ "error" must beEqualTo(JNothing)
      }

      "responds to autocomplete" in new GivenSocketServer with GivenApiClient {
        givenStartedServer
        val requestId: String = UUID.randomUUID().toString
        val payload = result(futureClient() flatMap sendAutoComplete(broker, requestId, 1,1, ""), fromSeconds(10))

        val json = parse(payload)

        json \ "error" must beEqualTo(JNothing)

        (json \ "type" values) must beEqualTo("auto-complete")

        private val completions: List[JValue] = (json \ "completions").children

        completions must beEmpty

      }

      "autocompletes basic scala code" in new GivenSocketServer with GivenApiClient with GivenBasicSource {
        givenStartedServer

        val requestId: String = UUID.randomUUID().toString
        val payload = result(futureClient() flatMap sendAutoComplete(broker, requestId, 1, 0, sourceDocument), fromSeconds(10))

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
          givenStartedServer.stop()

          def attemptConnect(): Unit = {
            val webSocket: WebSocket = result(futureClient(), fromSeconds(10))
          }

          attemptConnect must throwA[ChannelWriteException]
        }
      }
    }
  }
}