package com.thoughtworks.sbt.sbteo.steps

import com.thoughtworks.sbt.sbteo.TapAfter

import scala.collection.JavaConversions._

trait GivenRuntimeConfiguration extends TapAfter {

  def givenSystemProperty(name: String, value: String): Unit = {
    tapAfter[Option[AnyRef]](mapAsScalaMap(System.getProperties).get(name), {
      case Some(v) => System.setProperty(name, v.toString)
      case None => System.clearProperty(name.toString)
    })
    System.setProperty(name, value)
  }
}
