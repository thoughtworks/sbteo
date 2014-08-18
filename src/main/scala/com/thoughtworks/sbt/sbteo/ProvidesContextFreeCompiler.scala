package com.thoughtworks.sbt.sbteo

import scala.tools.nsc.Settings
import scala.tools.nsc.interactive.Global
import scala.tools.nsc.reporters.StoreReporter

trait ProvidesContextFreeCompiler extends RequiresCompiler with RequiresCleanup {
  lazy val theCompiler: Global = {
    def cleanGlobal(c: Global): Unit = {
      c.askShutdown()
    }
    val settings: Settings = new BasicSettings().settings

    val global = new Global(settings, new StoreReporter())

    onCleanup[Global](global, cleanGlobal)
  }


  def compiler: Global = {
    theCompiler
  }

}
