package com.thoughtworks.sbt.sbteo

/**
 * Created by ndrew on 14/08/2014.
 */
trait JsonApi {
  implicit val formats = net.liftweb.json.DefaultFormats

  def autocomplete(doc: Seq[String], position: CursorPosition): Seq[AutoCompletion]

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
      case ac@AutoCompleteRequest(_, _, doc, position, _) => new AutoCompleteResponse(ac, autocomplete(doc, position))
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


}