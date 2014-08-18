package com.thoughtworks.sbt.sbteo.steps

/**
 * Created by ndrew on 15/08/2014.
 */
trait GivenBasicSource {
  lazy val sourceDocument:String = {
    """
      |class X {
      |  def a(){
      |    /*!1!*/
      |    val x = 0;
      |    this./*!2!*/
      |  }
      |}
      |
      |
    """.stripMargin('|')
  }
}
