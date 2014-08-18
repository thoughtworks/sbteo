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

class SbteoWireProtocolActor extends Actor with JsonProtocol with JsonApi with RealApi with ProvidesContextFreeCompiler with ProvidesCleanup {

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

  override def postStop(): Unit = {
    cleanup()
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

  implicit class Regex(sc: StringContext) {
    def r = new util.matching.Regex(sc.parts.mkString, sc.parts.tail.map(_ => "x"): _*)
  }

  type EndpointType = Tuple2[String, Int]

  import scala.collection.JavaConversions._

  lazy val endpoint: EndpointType = {
    val endpointValue: Option[String] = mapAsScalaMap(System.getenv()).get("SBTEO_ENDPOINT").orElse {
      mapAsScalaMap(System.getProperties).get("sbteo.endpoint").map[String] { s => s.toString}
    }
    def tryParse(s: String): Option[EndpointType] = {
      s match {
        case r"(\w+)$iface\:(\d+)$port" => Some((iface, port.toInt))
        case r"(\w+)$iface" => Some((iface, 8888))
        case r":(\d+)$port" => Some(("localhost", port.toInt))
        case _ => None
      }
    }
    endpointValue flatMap tryParse getOrElse (("localhost", 8888))
  }

  def start(): SbteoServer = {
    server match {
      case Some(webServer) => this
      case None =>
        val actorSystem: ActorSystem = ActorSystem("SbteoServerActorSystem", ConfigFactory.load(), this.getClass.getClassLoader)
        println(endpoint)
        val config: WebServerConfig = new WebServerConfig(port = endpoint._2, hostname = endpoint._1)
        val webServer: WebServer = new WebServer(config, SbteoServer.routes(actorSystem), actorSystem)
        webServer.start()
        new SbteoServer(this.logger, Option(webServer))
    }
  }

  def stop(): Unit = {
    server.foreach(_.stop())
  }

}

