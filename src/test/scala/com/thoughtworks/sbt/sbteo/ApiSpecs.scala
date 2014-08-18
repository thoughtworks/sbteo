package com.thoughtworks.sbt.sbteo

import com.thoughtworks.sbt.sbteo.Api.{AutoCompletion, CursorPosition}
import com.thoughtworks.sbt.sbteo.steps.{GivenCompiler, GivenBasicSource, GivenApi}
import org.specs2.matcher.LeftCheckedMatcher
import org.specs2.mutable._
import org.specs2.specification.Scope

class ApiSpecs extends Specification {
  sequential
  "An api" should {
    "with basic settings" should {
      "autocomplete on a basic file" should {
        trait Subject extends GivenApi with GivenBasicSource with GivenCompiler with Scope {
          def suggestionsAt(row:Int, col: Int) = {
            api.autocomplete(sourceDocument.split("\n"), new CursorPosition(row,col))
          }
        }

        "the suggestions at import statement" should {
          "not suggest local vars" in new Subject {
            suggestionsAt(1,1).left.get.map(ac => ac.symbol) must not contain("x")
          }
          "suggest classes" in new Subject {
            suggestionsAt(1,1).left.get.map(ac => ac.symbol) must contain("X")
          }
        }
        "the suggestions at inside a() method" should {
          "suggest variables" in new Subject {
            suggestionsAt(5,9) must beLeft
          }
          "contain a local variable" in new Subject {
            suggestionsAt(5, 9).left.get.map(ac => ac.symbol) must contain("x")

          }
        }
      }
    }
  }
}
