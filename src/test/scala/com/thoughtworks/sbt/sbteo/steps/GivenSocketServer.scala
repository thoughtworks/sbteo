package com.thoughtworks.sbt.sbteo.steps

import com.thoughtworks.sbt.sbteo.stubs.StubLogger
import com.thoughtworks.sbt.sbteo.{SbteoServer, TapAfter}

trait GivenSocketServer extends StubLogger with TapAfter {

  lazy val socketServer: SbteoServer = tapAfter[SbteoServer](new SbteoServer(aStubLogger), _ => {})

  lazy val givenStartedServer: SbteoServer = tapAfter(socketServer.start(), _.stop())

}
