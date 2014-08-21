package com.thoughtworks.sbt.sbteo

import java.util.concurrent.{TimeoutException, TimeUnit, Callable, FutureTask}

import com.thoughtworks.sbt.sbteo.Api.{Documentation, AutoCompletion}

import scala.concurrent.duration.Duration
import scala.reflect.internal.util.BatchSourceFile
import scala.tools.nsc.interactive.{Response, Global}

/*
 * This code is heavily indebted to https://github.com/MasseGuillaume/ScalaKata
 * which in turn builds on ScalaIDE, and Ensime
 *
 */
case class TypeAtResponse(
                           tpe: String
                           )


class InprocessCompiler(compiler: Global, log: sbt.Logger) {

  private def reload(code: String): BatchSourceFile = {
    val file = new BatchSourceFile("default", code)
    withResponse[Unit](r ⇒ compiler.askReload(List(file), r)).get
    file
  }

  private def withResponse[A](op: Response[A] ⇒ Any): Response[A] = {
    val response = new Response[A]
    op(response)
    response
  }

  def autocomplete(code: String, p: Int): Either[Seq[AutoCompletion], Throwable] = {
    def completion(f: (compiler.Position, compiler.Response[List[compiler.Member]]) ⇒ Unit,
                   pos: compiler.Position): Either[Seq[AutoCompletion], Throwable] = {

      withResponse[List[compiler.Member]](r ⇒ f(pos, r)).get match {
        case Left(members) ⇒ compiler.ask(() ⇒ {
          Left(members.map(member ⇒ {
            AutoCompletion(
              symbol = member.sym.decodedName,
              signature = member.sym.signatureString,
              documentation = Documentation(""),
              tags = tagsForMember(member).toList,
              priority = if (member.implicitlyAdded) 2 else 1
            )
          }))
        })
        case Right(e) ⇒
          Right(e)
      }
    }
    def typeCompletion(pos: compiler.Position) = {
      completion(compiler.askTypeCompletion, pos).left.map(_.sortBy(_.priority))
    }

    def scopeCompletion(pos: compiler.Position) = {
      completion(compiler.askScopeCompletion, pos).left.map(_.sortBy(_.priority))
    }

    // inspired by scala-ide
    // https://github.com/scala-ide/scala-ide/blob/4.0.0-m3-luna/org.scala-ide.sdt.core/src/org/scalaide/core/completion/ScalaCompletions.scala#L170
    askTypeAt(code, p, p) {
      (tree, pos) ⇒ tree match {
        case compiler.New(name) ⇒
          log.debug {
            s"ast/New: $name"
          }
          typeCompletion(name.pos)
        case compiler.Select(qualifier, _) if qualifier.pos.isDefined && qualifier.pos.isRange ⇒
          log.debug {
            s"ast/Select: $qualifier"
          }
          typeCompletion(qualifier.pos)
        case compiler.Import(expr, _) ⇒
          log.debug {
            s"ast/Import: $expr"
          }
          typeCompletion(expr.pos)
        case compiler.Apply(fun, _) ⇒
          log.debug {
            s"ast/Apply: $fun"
          }
          fun match {
            case compiler.Select(qualifier: compiler.New, _) ⇒
              log.debug {
                s"ast/Apply/Select/New: $qualifier"
              }
              typeCompletion(qualifier.pos)
            case compiler.Select(qualifier, _) if qualifier.pos.isDefined && qualifier.pos.isRange ⇒
              log.debug {
                s"ast/Apply/Select: $qualifier"
              }
              typeCompletion(qualifier.pos)
            case x ⇒
              log.debug {
                s"ast: $x"
              }
              scopeCompletion(fun.pos)
          }
        case x ⇒
          log.debug {
            s"ast/Apply/Select: $x"
          }
          scopeCompletion(pos)
      }
    } {
      pos => Some(scopeCompletion(pos))
    }.getOrElse(Left(List()))
  }

  private type SymbolType = compiler.Symbol
  private val tagSpecs = Map(
    ((s: SymbolType) => s.isClass) -> "class",
    ((s: SymbolType) => s.isAnonymousFunction) -> "function",
    ((s: SymbolType) => s.isMethod) -> "method",
    ((s: SymbolType) => s.isAccessor) -> "getter",
    ((s: SymbolType) => s.isSetter) -> "setter",
    ((s: SymbolType) => s.isPrivate)-> "private",
    ((s: SymbolType) => s.isLocal) -> "local",
    ((s: SymbolType) => s.isPackage || s.isPackageObjectOrClass) -> "package"
  )
  def tagsForMember(member: compiler.Member): Seq[String] = {
    val sym: compiler.Symbol = member.sym

    tagSpecs.
      filter { case (k, v) => k(sym)}.
      map { case (k, v) => v}.
      toSeq
  }

  def typeAt(code: String, start: Int, end: Int): Option[TypeAtResponse] = {
    askTypeAt(code, start, end) { (tree, _) ⇒ {
      // inspired by ensime
      val res =
        tree match {
          case compiler.Select(qual, name) ⇒ qual
          case t: compiler.ImplDef if t.impl != null ⇒ t.impl
          case t: compiler.ValOrDefDef if t.tpt != null ⇒ t.tpt
          case t: compiler.ValOrDefDef if t.rhs != null ⇒ t.rhs
          case t ⇒ t
        }
      TypeAtResponse(res.tpe.toString())
    }
    } {
      Function.const(None)
    }
  }

  private def askTypeAt[A]
  (code: String, start: Int, end: Int)
  (f: (compiler.Tree, compiler.Position) ⇒ A)
  (fb: compiler.Position ⇒ Option[A]): Option[A] = {

    if (code.isEmpty) None
    else {
      val file = reload(code)
      val rpos = compiler.rangePos(file, start, start, end)

      val response = withResponse[compiler.Tree](r ⇒
        compiler.askTypeAt(rpos, r)
      )

      response.get match {
        case Left(tree) ⇒ Some(f(tree, rpos))
        case Right(e) ⇒
          e.printStackTrace()
          fb(rpos);
      }
    }
  }

  //  private val timeout = 60.seconds
  //  private val jvmId = java.lang.Math.abs(new Random().nextInt())


  //  private def convert(infos: Map[String, List[(Int, String)]]): Map[Severity, List[CompilationInfo]] = {
  //    infos.map { case (k, vs) ⇒
  //      val sev = k match {
  //        case "ERROR" ⇒ Error
  //        case "WARNING" ⇒ Warning
  //        case "INFO" ⇒ Info
  //      }
  //      (sev, vs map { case (p, m) ⇒ CompilationInfo(m, p)})
  //    }
  //  }

  private def withTimeout[T](f: ⇒ T)(timeout: Duration): Option[T] = {
    val task = new FutureTask(new Callable[T]() {
      def call = f
    })
    val thread = new Thread(task)
    try {
      thread.start()
      Some(task.get(timeout.toMillis, TimeUnit.MILLISECONDS))
    } catch {
      case e: TimeoutException ⇒ None
    } finally {
      if (thread.isAlive) {
        thread.interrupt()
        thread.stop()
      }
    }
  }
}
