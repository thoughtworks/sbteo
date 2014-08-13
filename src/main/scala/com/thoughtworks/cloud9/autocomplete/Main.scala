package com.thoughtworks.cloud9.autocomplete

import java.io.File
import java.net.URLClassLoader
import java.util.jar.JarFile

import scala.reflect.internal.util.BatchSourceFile
import scala.tools.nsc.Settings
import scala.tools.nsc.interactive.{Global, Response}
import scala.tools.nsc.io.VirtualDirectory
import scala.tools.nsc.reporters.ConsoleReporter
import scala.tools.nsc.util.OffsetPosition

class Main {

  /*
 * For a given FQ classname, trick the resource finder into telling us the containing jar.
 */
  private def classPathOfClass(className: String) = {
    val resource = className.split('.').mkString("/", "/", ".class")
    val path = getClass.getResource(resource).getPath
    if (path.indexOf("file:") >= 0) {
      val indexOfFile = path.indexOf("file:") + 5
      val indexOfSeparator = path.lastIndexOf('!')
      List(path.substring(indexOfFile, indexOfSeparator))
    } else {
      require(path.endsWith(resource))
      List(path.substring(0, path.length - resource.length + 1))
    }
  }

  private lazy val compilerPath = try {
    classPathOfClass("scala.tools.nsc.Interpreter")
  } catch {
    case e: Throwable =>
      throw new RuntimeException("Unable to load Scala interpreter from classpath (scala-compiler jar is missing?)", e)
  }

  private lazy val libPath = try {
    classPathOfClass("scala.ScalaObject")
  } catch {
    case e: Throwable =>
      throw new RuntimeException("Unable to load scala base object from classpath (scala-library jar is missing?)", e)
  }

  /*
 * Try to guess our app's classpath.
 * This is probably fragile.
 */
  lazy val impliedClassPath: List[String] = {
    def getClassPath(cl: ClassLoader, acc: List[List[String]] = List.empty): List[List[String]] = {
      val cp = cl match {
        case urlClassLoader: URLClassLoader => urlClassLoader.getURLs.filter(_.getProtocol == "file").
          map(u => u.getPath).toList
        case _ => Nil
      }
      cl.getParent match {
        case null => (cp :: acc).reverse
        case parent => getClassPath(parent, cp :: acc)
      }
    }

    val classPath = getClassPath(this.getClass.getClassLoader)
    val currentClassPath = classPath.head

    // if there's just one thing in the classpath, and it's a jar, assume an executable jar.
    currentClassPath ::: (if (currentClassPath.size == 1 && currentClassPath(0).endsWith(".jar")) {
      val jarFile = currentClassPath(0)
      val relativeRoot = new File(jarFile).getParentFile()
      val nestedClassPath = new JarFile(jarFile).getManifest.getMainAttributes.getValue("Class-Path")
      if (nestedClassPath eq null) {
        Nil
      } else {
        nestedClassPath.split(" ").map { f => new File(relativeRoot, f).getAbsolutePath }.toList
      }
    } else {
      Nil
    }) ::: classPath.tail.flatten
  }

  def main(args: Array[String]) {
    val target = new VirtualDirectory("", None)

    val settings = new Settings

    val pathList = compilerPath ::: libPath
    settings.outputDirs.setSingleOutput(target)
    settings.bootclasspath.value = pathList.mkString(File.pathSeparator)
    settings.classpath.value = (pathList ::: impliedClassPath).mkString(File.pathSeparator)

    val reporter = new ConsoleReporter(settings)
    val compiler = new Global(settings, reporter)

    val code =
      """
         |class X {
         |  def a(){
         |    /*!1!*/
         |    val x = 0;
         |    this./*!2!*/x
         |  }
         |}
         |
         |
      """.stripMargin('|')
    val source = new BatchSourceFile("<virtual>", code)
    val response = new Response[Unit]
    compiler.askReload(List(source), response)
    response.get.left.foreach( _ => println("yay!"))

    val tcompletion = new Response[List[compiler.Member]]
    val pos = compiler.ask(() => new OffsetPosition(source, code.indexOf("/*2*/")-1 ))


    compiler.askTypeCompletion(pos, tcompletion)
    tcompletion.get match {
      case Left(members) => compiler.ask(() => members.foreach( println ) )
      case Right(e) =>
        e.printStackTrace
    }
    compiler.askShutdown()
  }
}