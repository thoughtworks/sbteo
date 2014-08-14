package com.thoughtworks.sbt.sbteo

import java.util.UUID

import com.twitter.concurrent.Broker
import com.twitter.finagle.websocket.WebSocket
import com.twitter.finagle.{ChannelWriteException, HttpWebSocket}
import com.twitter.util.Await.result
import com.twitter.util.Duration.fromSeconds
import com.twitter.util.Future
import org.specs2.mutable._
import sbt.Level

trait StubLogger {

  class StubLogger extends Object with sbt.Logger {
    override def trace(t: => Throwable): Unit = {}

    override def log(level: Level.Value, message: => String): Unit = {}

    override def success(message: => String): Unit = {}
  }

  lazy val aStubLogger = {
    new StubLogger
  }
}

trait TapAfter extends After {
  private var afters: List[Function[Unit, Unit]] = List()

  def tapAfter[A](a: A, f: Function[A, Unit]) = {
    afters = afters :+ ((_: Unit) => {
      f(a)
    })
    a
  }

  def after = {
    afters.foreach(f => f())
  }

}

trait GivenSocketServer extends StubLogger with TapAfter {

  lazy val socketServer: SbteoServer = {
    tapAfter[SbteoServer](new SbteoServer(aStubLogger), _ => {})
  }

  lazy val startedServer: SbteoServer = {
    tapAfter[SbteoServer](socketServer.start(), s => {
      System.out.println("stopping");
      s.stop()
    })
  }

}

trait GivenApiClient {
  lazy val broker: Broker[String] = {
    new Broker[String]
  }

  def futureClient(): Future[WebSocket] = {
    HttpWebSocket.open(broker.recv, "ws://localhost:8888/sbt/")
  }
}

class SteoServerSpecification extends Specification {
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
        val requestId: UUID = UUID.randomUUID()
        val expectedRequest: String = "\"requestId\":\"%s\"".format(requestId)
        var expectedType: String = """"type":"ping""""

        private val payload: String = result(futureClient() flatMap { ws =>
          broker !! s"""{"requestId":"$requestId","type":"ping"}"""
          ws.messages.?
        }, fromSeconds(1))

        payload must contain(expectedRequest)
        payload must contain(expectedType)
        payload must not contain (""""error":""")
      }

      "responds to autocomplete" in new GivenSocketServer with GivenApiClient{
        startedServer
        val requestId: UUID = UUID.randomUUID()
        val payload = result(futureClient() flatMap { ws =>
          broker !!
            s"""{
               |"requestId":"$requestId",
               |"type":"auto-complete",
               |"doc":[],
               |"position":{
               |   "row":0,
               |   "column":0
               |  }
               |}""".stripMargin
          ws.messages.?
        }, fromSeconds(1))

        payload must contain(""""type":"auto-complete"""")
        payload must contain(""""completions":[""")
        payload must not contain """"error"""
      }

      "when stopped" should {
        "not accept socket connections" in new GivenSocketServer with GivenApiClient{
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