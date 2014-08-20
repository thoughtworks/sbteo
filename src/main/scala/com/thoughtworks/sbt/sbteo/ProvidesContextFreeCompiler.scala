package com.thoughtworks.sbt.sbteo

import java.io.File

import scala.reflect.io.VirtualDirectory
import scala.tools.nsc.Settings
import scala.tools.nsc.interactive.Global
import scala.tools.nsc.reporters.StoreReporter

trait ProvidesCompiler extends RequiresCompiler {
  def compiler: Global
}

trait RequiresCompilerSettings {
  def compilerSettings: Settings
}

trait ProvidesBasicCompilerSettings extends RequiresCompilerSettings {
  lazy val compilerSettings: Settings = new BasicSettings().settings
}

trait RequiresCompilerClassPath {
  def compilerClassPath:Seq[File]
}
trait ProvidesFullCompilerSettings extends RequiresCompilerSettings with RequiresCompilerClassPath {
  lazy val compilerSettings: Settings = {
    val settings = new Settings()
    val target = new VirtualDirectory("(memory)", None)

//    val pathList = sbt.Keys.compilerPath ::: libPath

    val artifacts:String=compilerClassPath.
      map(_.getAbsoluteFile).
      mkString(File.pathSeparator)

    settings.outputDirs.setSingleOutput(target)
    settings.bootclasspath.value = artifacts
    settings.classpath.value = artifacts
    settings
  }
}
trait ProvidesContextFreeCompiler extends ProvidesCompiler with RequiresCompilerSettings with RequiresCleanup {
  lazy val compiler: Global = {
    def cleanGlobal(c: Global): Unit = {
      c.askShutdown()
    }

    val global = new Global(compilerSettings, new StoreReporter())

    onCleanup[Global](global, cleanGlobal)
  }

}
