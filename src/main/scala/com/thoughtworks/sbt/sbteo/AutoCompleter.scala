package com.thoughtworks.sbt.sbteo

import com.thoughtworks.sbt.sbteo.Api.{AutoCompletion, CursorPosition}


class AutoCompleter(compiler: InprocessCompiler) {
  def autocomplete(code: Seq[String], position: CursorPosition): Either[Seq[AutoCompletion], Throwable] = {
    compiler.autocomplete(code.mkString("\n"), position.positionIn(code))
  }
}
