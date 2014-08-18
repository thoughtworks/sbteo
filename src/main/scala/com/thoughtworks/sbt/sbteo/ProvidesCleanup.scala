package com.thoughtworks.sbt.sbteo

trait ProvidesCleanup extends RequiresCleanup {
  private var cleanupActions: List[Function[Unit, Unit]] = List()

  def onCleanup[A](a: A, f: Function[A, Unit]) = {
    cleanupActions = cleanupActions :+ ((_: Unit) => {
      f(a)
    })
    a
  }

  def cleanup() = {
    val _actions = cleanupActions
    cleanupActions = List()
    _actions.reverse.foreach(f => f())
  }


}
