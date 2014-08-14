package com.thoughtworks.sbt.sbteo

import java.text.{DateFormat, SimpleDateFormat}
import java.util.{Calendar, GregorianCalendar}

import akka.actor.{Actor, ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import net.liftweb.json._
import org.mashupbots.socko.events.{HttpRequestEvent, SockoEvent, WebSocketFrameEvent}
import org.mashupbots.socko.routes._
import org.mashupbots.socko.webserver.{WebServer, WebServerConfig}

object WebSocketHandler {
  val dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
  val time = new GregorianCalendar()

  def props(): Props = {
    Props(new WebSocketHandler)
  }
}

class WebSocketHandler extends Actor with JsonApi {
  def emitJson(event: WebSocketFrameEvent)(json: JValue): Unit = {
    event.writeText(pretty(render(json)))
  }

  def receive = {
    case event: HttpRequestEvent =>
      event.response.write("sbteo")
      context.stop(self)
    case event: WebSocketFrameEvent =>
      if (event.isText) {
        jsonApi(parse(event.readText()), emitJson(event))
      }
      context.stop(self)
    case _ =>
      context.stop(self)
  }
}

object SbteoServer {
  def routes(actorSystem: ActorSystem): PartialFunction[SockoEvent, Unit] = {
    Routes({
      case WebSocketHandshake(wsHandshake) => wsHandshake match {
        case Path("/sbt/") =>
          wsHandshake.authorize()
      }
      case WebSocketFrame(wsFrame) =>
        actorSystem.actorOf(WebSocketHandler.props()) ! wsFrame
    })
  }
}

class SbteoServer(val logger: sbt.Logger, val server: Option[WebServer] = None) {
  def start(): SbteoServer = {
    server match {
      case Some(webServer) => this
      case None =>
        val actorSystem: ActorSystem = ActorSystem("SbteoServerActorSystem", ConfigFactory.load(), this.getClass.getClassLoader)
        val webServer: WebServer = new WebServer(new WebServerConfig(), SbteoServer.routes(actorSystem), actorSystem)
        webServer.start()
        new SbteoServer(this.logger, Option(webServer))
    }
  }

  def stop(): Unit = {
    server.foreach(_.stop())
  }

}

