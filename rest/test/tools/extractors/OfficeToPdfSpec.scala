package tools.extractors

import org.specs2.mutable._
import org.mockito.Matchers._
import org.specs2.mock._
import tools.storage.Storage
import tools.{Context, Configurable}
import org.artofsolving.jodconverter.OfficeDocumentConverter
import java.io.{File, FileInputStream}
import tools.dto.{Png, Meta}

class OfficeToPdfSpec extends SpecificationWithJUnit with Specification with Mockito {

	type ODC = OfficeDocumentConverter
	val f1 = OfficeToPdf.makeConverter

	"Extract method" should {
		"stop processing if office conversion fails" in {
			val storage = mock[Storage]
			val key1 = "key1"
			val is = new FileInputStream(OfficeToPdf.tmpFile(".test"))
			val converter = mock[ODC]

			OfficeToPdf.makeConverter = () => converter

			OfficeToPdf.context = mock[Configurable]
			OfficeToPdf.context.getStorage returns Some (storage) 
			storage.getStream (key1) returns Some (is)
			converter.convert (any[File], any[File]) throws new IllegalStateException("No way")

			OfficeToPdf.extract(key1) must_== None
			there was one (converter).convert (any[File], any[File])
		}

		"finish processing by pdf conversion" in {
			val storage = mock[Storage]
			val key1 = "key1"
			val is = new FileInputStream(OfficeToPdf.tmpFile(".test"))
			val converter = mock[ODC]
			val meta = Meta(List(Png(1,"","")))

			OfficeToPdf.makeConverter = () => converter
			OfficeToPdf.pdf = mock[PdfCapable]

			OfficeToPdf.context = mock[Configurable]
			OfficeToPdf.context.getStorage returns Some (storage)
			storage.getStream (key1) returns Some (is)
			OfficeToPdf.pdf.toPng (anyString, any[File]) returns Some(meta)

			var result = OfficeToPdf.extract(key1)
			result must_!= None
			result.get must_== meta

			there was one (converter).convert (any[File], any[File])
			there was one (OfficeToPdf.pdf).toPng (anyString, any[File])
		}
	}

	OfficeToPdf.pdf = PdfToPng
	OfficeToPdf.context = Context
	OfficeToPdf.makeConverter = f1
}