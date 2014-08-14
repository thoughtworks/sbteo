package com.thoughtworks.sbt.sbteo.steps

import com.thoughtworks.sbt.sbteo.stubs.StubLogger
import com.thoughtworks.sbt.sbteo.{SbteoServer, TapAfter}

/**
 * Created by ndrew on 14/08/2014.
 */
trait GivenSocketServer extends StubLogger with TapAfter {

  lazy val socketServer: SbteoServer = {
    tapAfter[SbteoServer](new SbteoServer(aStubLogger), _ => {})
  }

  lazy val startedServer: SbteoServer = {
    tapAfter[SbteoServer](socketServer.start(), s => {
      System.out.println("stopping");
      s.stop()
    })
  }

}
