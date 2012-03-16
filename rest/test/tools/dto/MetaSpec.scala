package tools.dto

import org.specs2.mutable.{Specification, SpecificationWithJUnit}
import org.specs2.mock.Mockito
import collection.immutable.List
import play.api.libs.json.{JsValue, JsArray, Json}


class MetaSpec extends SpecificationWithJUnit with Specification with Mockito {
	"Json conversion" should {
		"work as expected" in {
			val meta1 = Meta (
				List ( Png (1, "k1", "pk1"), Png (2, "k2", "pk2")),
				Meta.InProgress
			)

			val json1 = Json parse Json.stringify(meta1.toJson)
			val pages = (json1 \ "pages").asOpt[List[JsValue]]
			(json1 \ "status").as[String] must_== Meta.InProgress
			pages.isDefined must_== true
			pages.get must have size (2)

			(pages.get(0) \ "page").as[Int] must_== 1
			(pages.get(1) \ "page").as[Int] must_== 2

			(pages.get(0) \ "key").as[String] must_== "k1"
			(pages.get(1) \ "key").as[String] must_== "k2"

			(pages.get(0) \ "previewKey").as[String] must_== "pk1"
			(pages.get(1) \ "previewKey").as[String] must_== "pk2"

			//Thread.sleep(1000)

			((pages.get(0) \ "time").as[Long] * 1000) < System.currentTimeMillis() must_== true
			((pages.get(1) \ "time").as[Long] * 1000) < System.currentTimeMillis() must_== true
			((json1 \ "time").as[Long] * 1000) < System.currentTimeMillis() must_== true
		}
	}
}
