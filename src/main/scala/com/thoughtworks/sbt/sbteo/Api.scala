package com.thoughtworks.sbt.sbteo

import com.thoughtworks.sbt.sbteo.Api._

import scala.collection.SeqView


object Api {

  case class CursorPosition(row: Int, column: Int) {
    val zeroIndexedRow = row-1
    val zeroIndexedCol = column -1
    
    def positionIn(sourceDocument: Seq[String]): Int = {
      def truncateRow(s:String, idx:Int): Int ={
        if( idx < zeroIndexedRow){
          s.length() +1
        }
        else {
          s.take(zeroIndexedCol).length()
        }
      }
      val sumRowLength: (Int, (String, Int)) => Int = {
        case (acc, (e, i)) => acc + truncateRow(e, i)
        case (acc, _) => acc
      }
      sourceDocument.take(row).view.zipWithIndex.foldLeft(1)(sumRowLength)
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
