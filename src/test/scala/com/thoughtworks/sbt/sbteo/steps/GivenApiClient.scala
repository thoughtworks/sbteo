package com.thoughtworks.sbt.sbteo.steps

import com.twitter.concurrent.Broker
import com.twitter.finagle.HttpWebSocket
import com.twitter.finagle.websocket.WebSocket
import com.twitter.util.Future

trait GivenApiClient {
  lazy val broker: Broker[String] = {
    new Broker[String]
  }

  def futureClient(): Future[WebSocket] = {
    HttpWebSocket.open(broker.recv, "ws://localhost:8888/sbt/")
  }
}
