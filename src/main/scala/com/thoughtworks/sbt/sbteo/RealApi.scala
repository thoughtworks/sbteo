package com.thoughtworks.sbt.sbteo

import com.thoughtworks.sbt.sbteo.Api.{AutoCompletion, CursorPosition}


trait RealApi extends Api with RequiresCompiler {

  lazy val theCompleter = new AutoCompleter(new InprocessCompiler(compiler))

  def completer: AutoCompleter = theCompleter

  def autocomplete(doc: Seq[String], position: CursorPosition): Either[Seq[AutoCompletion], Throwable] = {
    completer.autocomplete(doc, position)
  }

}
