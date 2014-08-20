package com.thoughtworks.sbt.sbteo

import com.thoughtworks.sbt.sbteo.Api._
import com.thoughtworks.sbt.sbteo.steps._
import org.specs2.mutable._
import org.specs2.specification.Scope

class CursorPositionSpecs extends Specification {
  sequential
  "A CursorPosition" should {
    trait SourceSubject {
      def sourceDocument: String

      def textAtSource(row:Int, col:Int): Char = {
        sourceDocument.charAt(positionOf(row, col))
      }
      def positionOf(row: Int, col: Int): Int = {
        CursorPosition(row, col).positionIn(sourceDocument.split("\n"))
      }
    }
    "with an empty source document" should {
      trait Subject extends SourceSubject with Scope {
        def sourceDocument = ""
      }
      "at position 0 0" in new Subject {
        positionOf(0, 0) must beEqualTo(0)
      }
      "at position 1 1" in new Subject {
        positionOf(1, 1) must beEqualTo(0)
      }

      "at position 0 9" in new Subject {
        positionOf(0, 9) must beEqualTo(0)
      }

    }
    "with a source document" should {
      trait Subject extends SourceSubject with GivenBasicSource with Scope
      "text" should {
        "at position 0 0 = 'c'" in new Subject {
          textAtSource(0,0) must be_==('\n')
        }
        "at position 1 1" in new Subject {
          textAtSource(1,1) must be_==('l')
        }
        "at position 1 3" in new Subject {
          textAtSource(1,3) must be_==('s')
        }
        "at position 2 3" in new Subject {
          textAtSource(2,3) must be_==('d')
        }
      }
      "cursor position" should {
        "at position 0 0" in new Subject {
          positionOf(0, 0).must(be_==(0))
        }

        "at position 1 0" in new Subject {
          positionOf(1, 0) must be_==(1)
        }
        "at position 1 1" in new Subject {
          positionOf(1, 1) must be_==(positionOf(1, 0) + 1)
        }

        "near EOL" in new Subject {
          positionOf(1, 9) must be_==(positionOf(1, 8) + 1)
        }
        "near EOL (explicit)" in new Subject {
          positionOf(1, 9) must beEqualTo(10)
        }
        "on EOL" in new Subject {
          positionOf(1, 10) must be_==(positionOf(1, 9))
        }
        "on SOL" in new Subject {
          positionOf(2, 0) must be_==(positionOf(1, 9))
        }

      }


    }


  }
}
