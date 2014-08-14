package com.thoughtworks.sbt.sbteo

/**
 * Created by ndrew on 14/08/2014.
 */
trait TapAfter extends After {
  private var afters: List[Function[Unit, Unit]] = List()

  def tapAfter[A](a: A, f: Function[A, Unit]) = {
    afters = afters :+ ((_: Unit) => {
      f(a)
    })
    a
  }

  def after = {
    afters.foreach(f => f())
  }

}
