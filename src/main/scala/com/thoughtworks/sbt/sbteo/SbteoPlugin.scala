package com.thoughtworks.sbt.sbteo

import java.util.concurrent.atomic.AtomicReference

import sbt.Keys._
import sbt._

object SbteoPlugin extends sbt.AutoPlugin {

  val sbteo = config("sbteo").hide

  val start = TaskKey[SbteoServer]("start")
  val stop = TaskKey[Unit]("stop")
  //lazy val launcher = TaskKey[Seq[String]]("launcher")



  private def shutdown(l: Logger, atomicRef: AtomicReference[Option[SbteoServer]]): Unit = {
    val oldProcess = atomicRef.getAndSet(None)
    oldProcess.foreach(stopServer(l))
  }

  private def stopServer(l: Logger)(p: SbteoServer): Unit = {
    l.info("waiting for server to shut down...")
    p.stop()
  }

  def onLoadSetting(atomicRef: AtomicReference[Option[SbteoServer]]): Def.Initialize[State => State] = Def.setting {
    (onLoad in Global).value compose { state: State =>
      state.addExitHook(shutdown(state.log, atomicRef))
    }
  }

  def startup(logger: Logger, classPath: Seq[File]): SbteoServer = {
    new SbteoServer(logger).start()
  }

  def startTask(reference: AtomicReference[Option[SbteoServer]])(classPath:Classpath, streams:TaskStreams): SbteoServer = {
      shutdown(streams.log, reference)
      val server = startup(streams.log, classPath.map(_.data))
      reference.set(Option(server))
      server
  }


  def stopTask(reference: AtomicReference[Option[SbteoServer]]): Def.Initialize[Task[Unit]] = Def.task {
    shutdown(streams.value.log, reference)
  }

  lazy val baseSbteoSettings: Seq[sbt.Def.Setting[_]] = {
    val atomicRef: AtomicReference[Option[SbteoServer]] = new AtomicReference(None)
    System.out println "sbteo baseSettings evaluated"

    inConfig(sbteo) {
      Seq(start <<= (fullClasspath in Compile, streams) map startTask(atomicRef)
        , stop <<= stopTask(atomicRef)
        , onLoad in Global <<= onLoadSetting(atomicRef)
      )
    }
  }

  override val projectSettings: Seq[sbt.Def.Setting[_]] = baseSbteoSettings

  override def trigger = allRequirements
}
