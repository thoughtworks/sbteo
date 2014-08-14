package com.thoughtworks.sbt.sbteo.stubs

import sbt.Level

/**
 * Created by ndrew on 14/08/2014.
 */
trait StubLogger {

  class StubLogger extends Object with sbt.Logger {
    override def trace(t: => Throwable): Unit = {}

    override def log(level: Level.Value, message: => String): Unit = {}

    override def success(message: => String): Unit = {}
  }

  lazy val aStubLogger = {
    new StubLogger
  }
}
