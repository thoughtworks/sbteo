package com.thoughtworks.sbt.sbteo

import com.thoughtworks.sbt.sbteo.Api._
import com.thoughtworks.sbt.sbteo.steps._
import org.specs2.mutable._
import org.specs2.specification.Scope

class CursorPositionSpecs extends Specification {
  "A CursorPosition" should {
    trait Subject extends GivenBasicSource with Scope {
      def positionOf(row: Int, col: Int): Int = {
        CursorPosition(row, col).positionIn(sourceDocument.split("\n"))
      }
    }
    "at position 1 1" in new Subject {
      private val actual: Int = positionOf(1, 1)
      actual.must(be_==(1))
    }
    "at position 2 1" in new Subject {
      positionOf(2, 1) must be_==(2)
    }
    "at position 2 2" in new Subject {
      positionOf(2, 2) must be_==(positionOf(2,1)+1)
    }
    "near EOL" in new Subject {
      positionOf(2, 10) must be_==(positionOf(2,9)+1)
    }
    "on EOL" in new Subject {
      positionOf(2, 11) must be_==(positionOf(2,10))
    }
    "on SOL" in new Subject {
      positionOf(3, 1) must be_==(positionOf(2,10)+1)
    }

  }
}
