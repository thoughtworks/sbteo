package com.thoughtworks.sbt.sbteo.steps

import com.thoughtworks.sbt.sbteo._

import scala.tools.nsc.interactive.Global


trait GivenCompiler extends TapAfter {

  type CompilerComponent = ProvidesContextFreeCompiler with ProvidesCleanup

  lazy val theCompiler: Global = {
    tapAfter[CompilerComponent](new ProvidesContextFreeCompiler with ProvidesCleanup, s => s.cleanup()).compiler
  }


  def compiler: Global = {
    theCompiler
  }
}
