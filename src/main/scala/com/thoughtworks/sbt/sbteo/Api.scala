package com.thoughtworks.sbt.sbteo

import com.thoughtworks.sbt.sbteo.Api._


object Api {

  case class CursorPosition(row: Int, column: Int)

  case class CompilationUnit(server_path: Option[String], syntax: Option[String])

  case class AutoCompletion(symbol: String,
                            signature: String,
                            documentation: Documentation,
                            tags: Seq[String],
                            priority: Int)

  case class Documentation(summary: String)

}

trait Api {

  def autocomplete(doc: Seq[String], position: CursorPosition): Seq[AutoCompletion] = {
    List(new AutoCompletion(
      "addExitHook",
      "addExitHook(f => Unit)",
      new Documentation("register callback to be called on VM shutdown"),
      List("function", "public"),
      1))
  }


}
