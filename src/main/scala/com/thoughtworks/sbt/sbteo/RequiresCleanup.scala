package com.thoughtworks.sbt.sbteo

trait RequiresCleanup {
  def onCleanup[A](a: A, f: Function[A, Unit]):A
}
