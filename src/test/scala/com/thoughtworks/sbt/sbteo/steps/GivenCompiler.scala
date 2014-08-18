package com.thoughtworks.sbt.sbteo.steps

import com.thoughtworks.sbt.sbteo.{BasicSettings, TapAfter}

import scala.tools.nsc.Settings
import scala.tools.nsc.interactive.Global
import scala.tools.nsc.reporters.{StoreReporter, ConsoleReporter}

trait GivenCompiler extends TapAfter {

  lazy val reporter:StoreReporter = {
    new StoreReporter()
  }

   lazy val theCompiler = {
     def afterCompiler(c:Global):Unit = {
       c.askShutdown()
     }
     val settings:Settings = new BasicSettings().settings

     val compiler = new Global(settings, reporter)

     tapAfter[Global](compiler,afterCompiler)
   }


   def compiler:Global = {
     theCompiler
   }
}
