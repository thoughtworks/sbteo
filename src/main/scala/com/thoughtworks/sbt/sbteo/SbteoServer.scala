package com.thoughtworks.sbt.sbteo

import java.text.SimpleDateFormat
import java.util.GregorianCalendar

import akka.actor.{Actor, ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import net.liftweb.json._
import org.mashupbots.socko.events.{HttpRequestEvent, SockoEvent, WebSocketFrameEvent}
import org.mashupbots.socko.routes._
import org.mashupbots.socko.webserver.{WebServer, WebServerConfig}

object SbteoWireProtocolActor {
  val dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
  val time = new GregorianCalendar()

  def props(): Props = {
    Props(new SbteoWireProtocolActor)
  }
}

trait JsonProtocol {
  def emitJson(writer: Function[String, Unit])(json: JValue): Unit = {
    writer(pretty(render(json)))
  }

  def jsonApi(req: JValue, next: Function[JValue, Unit]): Unit

  def handleJson(json: String, writer: Function[String, Unit]): Unit = {
    jsonApi(parse(json), emitJson(writer))
  }
}

class SbteoWireProtocolActor extends Actor with JsonProtocol with JsonApi with Api {

  def receive = {
    case event: HttpRequestEvent =>
      event.response.write("sbteo")
      context.stop(self)
    case event: WebSocketFrameEvent =>
      if (event.isText) {
        handleJson(event.readText(), event.writeText)
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
        actorSystem.actorOf(SbteoWireProtocolActor.props()) ! wsFrame
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

