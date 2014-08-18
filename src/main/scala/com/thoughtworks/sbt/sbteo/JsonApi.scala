package com.thoughtworks.sbt.sbteo

import java.util.UUID

import com.thoughtworks.sbt.sbteo.Api._
import com.thoughtworks.sbt.sbteo.JsonApi._
import net.liftweb.json._

trait JsonApi {
  implicit val formats = net.liftweb.json.DefaultFormats

  def autocomplete(doc: Seq[String], position: CursorPosition): Either[Seq[AutoCompletion], Throwable]

  def jsonApi(req: JValue, next: Function[JValue, Unit]): Unit = {
    val unkRequest: UnknownRequest = req.extract[UnknownRequest]
    val writer: Function[SbteoResponse, Unit] = (res) => next(Extraction.decompose(res))
    unkRequest.`type` match {
      case "auto-complete" => api(req.extract[AutoCompleteRequest], writer)
      case "ping" => api(req.extract[PingRequest], writer)
      case _ => api(unkRequest, (res) => next(Extraction.decompose(res)))
    }
  }

  def api(req: SbteoRequest, f: Function[SbteoResponse, Unit]): Unit = {
    f(req match {
      case ac@AutoCompleteRequest(_, _, doc, position, _) => {
        autocomplete(doc, position) match {
          case Left(result) => new AutoCompleteResponse(ac, result)
          case Right(e) => new ErrorResponse(ac, e)
        }
      }
      case ping@PingRequest(_, _) => new PingResponse(ping)
      case unk => new UnknownResponse(unk)
    })
  }


}

object JsonApi {

  trait SbteoRequest {
    val requestId: String
    val `type`: String
  }

  trait SbteoResponse {
    val requestId: String
    val responseId: String
    val `type`: String
  }

  case class AutoCompleteResponse(requestId: String,
                                  responseId: String,
                                  `type`: String,
                                  completions: Seq[AutoCompletion]) extends SbteoResponse {
    def this(req: AutoCompleteRequest, completions: Seq[AutoCompletion]) = this(req.requestId, UUID.randomUUID().toString, req.`type`, completions)
  }

  case class AutoCompleteRequest(requestId: String,
                                 `type`: String,
                                 doc: Seq[String],
                                 position: CursorPosition,
                                 compilation_unit: Option[CompilationUnit]) extends SbteoRequest {
  }

  case class PingRequest(requestId: String,
                         `type`: String) extends SbteoRequest {
  }

  case class PingResponse(requestId: String,
                          responseId: String,
                          `type`: String
                           ) extends SbteoResponse {
    def this(req: SbteoRequest) = this(req.requestId, UUID.randomUUID().toString, req.`type`)
  }


  case class UnknownRequest(requestId: String,
                            `type`: String) extends SbteoRequest {
  }

  case class UnknownResponse(requestId: String,
                             responseId: String,
                             `type`: String,
                             error: String = "unknown request"
                              ) extends SbteoResponse {
    def this(req: SbteoRequest) = this(req.requestId, UUID.randomUUID().toString, req.`type`)
  }

  case class ErrorResponse(requestId:String, responseId: String, e:Throwable, `type`: String = "error") extends SbteoResponse{
    def this(req: SbteoRequest, e:Throwable) = this(req.requestId, UUID.randomUUID().toString, e)

  }


}