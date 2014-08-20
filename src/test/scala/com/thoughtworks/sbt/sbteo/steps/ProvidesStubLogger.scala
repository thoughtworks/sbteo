package com.thoughtworks.sbt.sbteo.steps

import com.thoughtworks.sbt.sbteo.RequiresLogger
import com.thoughtworks.sbt.sbteo.stubs.StubLogger
import sbt.Logger

trait ProvidesStubLogger extends RequiresLogger with StubLogger{
  override def logger: Logger = aStubLogger
}
