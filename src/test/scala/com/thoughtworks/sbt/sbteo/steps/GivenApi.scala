package com.thoughtworks.sbt.sbteo.steps

import com.thoughtworks.sbt.sbteo.Api.{AutoCompletion, CursorPosition}
import com.thoughtworks.sbt.sbteo.{Api, AutoCompleter, InprocessCompiler}

import scala.tools.nsc.interactive.Global

trait GivenApi {
  def compiler:Global

  lazy val api:Api = {
    val completer: AutoCompleter = new AutoCompleter(new InprocessCompiler(compiler))
    new Api {
      override def autocomplete(doc: Seq[String], position: CursorPosition): Either[Seq[AutoCompletion], Throwable] = {
        completer.autocomplete(doc, position)
      }
    }
  }
}
