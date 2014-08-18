package com.thoughtworks.sbt.sbteo.steps

trait GivenBasicSource {
  def sourceDocument:String = {
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
