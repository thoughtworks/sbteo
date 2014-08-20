package com.thoughtworks.sbt.sbteo.steps

import com.thoughtworks.sbt.sbteo.{Api, RealApi}

import scala.tools.nsc.interactive.Global


trait GivenApi {
  self =>

  def compiler: Global

  lazy val api: Api = {
    new RealApi with ProvidesStubLogger {
      def compiler: Global = self.compiler
    }
  }
}
