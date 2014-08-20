package com.thoughtworks.sbt.sbteo

import com.thoughtworks.sbt.sbteo.Api._


object Api {

  case class CursorPosition(row: Int, column: Int) {
    val zeroIndexedRow = row
    val zeroIndexedCol = column

    def positionIn(sourceDocument: Seq[String]): Int = {
      def truncateRow(s: String, idx: Int): Int = {
        val hasPreviousLine = if (idx == 0) 0 else 1
        if (idx < zeroIndexedRow) {
          s.length()
        }
        else {
          s.take(zeroIndexedCol).length()
        } + hasPreviousLine
      }
      val sumRowLength: (Int, (String, Int)) => Int = {
        case (acc, (e, i)) => acc + truncateRow(e, i)
        case (acc, _) => acc
        case _ => 0
      }
      sourceDocument.take(zeroIndexedRow+1).view.zipWithIndex.foldLeft(0)(sumRowLength)
    }
  }

  case class CompilationUnit(server_path: Option[String], syntax: Option[String])

  case class AutoCompletion(symbol: String,
                            signature: String,
                            documentation: Documentation,
                            tags: Seq[String],
                            priority: Int)

  case class Documentation(summary: String)

}

trait Api {
  def autocomplete(doc: Seq[String], position: CursorPosition): Either[Seq[AutoCompletion], Throwable]
}

trait StubApi {
  def autocomplete(doc: Seq[String], position: CursorPosition): Either[Seq[AutoCompletion], Throwable] = {
    Left(List(new AutoCompletion(
      "addExitHook",
      "addExitHook(f => Unit)",
      new Documentation("register callback to be called on VM shutdown"),
      List("function", "public"),
      1)))
  }

}
