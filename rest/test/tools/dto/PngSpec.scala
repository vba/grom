package tools.dto

import org.specs2.mutable.{Specification, SpecificationWithJUnit}
import org.specs2.mock.Mockito
import play.api.libs.json.Json


class PngSpec extends SpecificationWithJUnit with Specification with Mockito {
	"Json conversion" should {
		"work as expected" in {
			val png = Png (11,"key","preview")
			val json = Json parse Json.stringify(png.toJson)

			(json \ "page").as[Int] must_== 11
			(json \ "key").as[String] must_== "key"
			(json \ "previewKey").as[String] must_== "preview"
		}
	}
}
