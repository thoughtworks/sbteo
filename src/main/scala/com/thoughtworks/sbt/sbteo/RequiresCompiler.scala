package com.thoughtworks.sbt.sbteo

import scala.tools.nsc.interactive.Global

trait RequiresCompiler {
  def compiler: Global
}
