import org.scalatest._

class BendisSpec extends FlatSpec with Matchers {
    "A compiler" should "autocomplete" in {
      new com.thoughtworks.cloud9.autocomplete.Main().main(Array("Aahhh"))
    }
}