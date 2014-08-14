package com.thoughtworks.sbt.sbteo.steps

import com.twitter.concurrent.Broker
import com.twitter.finagle.HttpWebSocket
import com.twitter.finagle.websocket.WebSocket
import com.twitter.util.Future

/**
 * Created by ndrew on 14/08/2014.
 */
trait GivenApiClient {
  lazy val broker: Broker[String] = {
    new Broker[String]
  }

  def futureClient(): Future[WebSocket] = {
    HttpWebSocket.open(broker.recv, "ws://localhost:8888/sbt/")
  }
}