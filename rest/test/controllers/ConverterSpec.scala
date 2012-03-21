package controllers

import org.specs2.mutable._
import org.mockito.Matchers._
import org.specs2.mock._
import scala.collection.JavaConversions._
import tools.storage.Storage
import tools.{Context, Configurable}
import collection.mutable.Set
import tools.dto.{OfficeKey, Key, PdfKey}
import tools.extractors.PdfToPng
import java.io.InputStream
import play.api.mvc.SimpleResult
import play.api.mvc._


class ConverterSpec extends SpecificationWithJUnit with Specification with Mockito {

	"Try extract pages" should {
		"program an extraction by known type correctly" in {

			val storage = mock[Storage]
			val keys = Set.empty[Key]

			Converter.context = mock[Configurable]
			Converter.context.getStorage returns Some(storage)
			Converter.context.getKeysToProcess returns keys

			Converter.extractPages("some1","application/newtype")
			Converter.extractPages("some2","application/pdf")
			Converter.extractPages("some3","application/msword")

			keys.filter(_.id == "some1").isEmpty must_== true
			keys.contains (PdfKey("some2")) must_== true
			keys.contains (OfficeKey("some3")) must_== true
		}

		"tries to get or program an extraction if key is not processed" in {

			val storage = mock[Storage]
			val keys = Set.empty[Key]
			val key1 = "1"
			val key2 = "2"

			Converter.context = mock[Configurable]
			Converter.context.getStorage returns Some(storage)
			Converter.context.getKeysToProcess returns keys

			storage has key1+PdfToPng.metaSuffix returns true
			storage getStream key1+PdfToPng.metaSuffix returns Some(mock[InputStream])

			val pages1 = Converter.pages(key1, "application/pdf")

			pages1 must_!= null
			pages1.isInstanceOf[SimpleResult[String]] must_== true
			val header1 = pages1.asInstanceOf[SimpleResult[String]].header
			header1.status must_== 200
			header1.headers.containsKey("Content-Type") must_== true
			header1.headers.containsValue("application/json") must_== true
			keys.size must_== 0

			storage has key2+PdfToPng.metaSuffix returns false
			val pages2 = Converter.pages(key2, "application/newtype").asInstanceOf[SimpleResult[String]]
			
			pages2.header.status must_== 415
			keys.size must_== 0

			val pages3 = Converter.pages(key2, "application/pdf").asInstanceOf[SimpleResult[String]]

			keys.contains(PdfKey(key2+PdfToPng.metaSuffix))
			pages3.header.status must_== 404

			val pages4 = Converter.pages(key2, "application/msword").asInstanceOf[SimpleResult[String]]
			keys.contains(OfficeKey(key2+PdfToPng.metaSuffix))
			pages4.header.status must_== 404
		}
	}
}
