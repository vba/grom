package controllers

import org.specs2.mutable._
import org.mockito.Matchers._
import org.specs2.mock._
import tools.Context
import play.api.Configuration
import java.io.File
import tools.storage.FileSystem
import play.api.libs.json.Json

class ConverterSpec extends Specification with Mockito {

	val base = new File(".").getCanonicalPath

	"Convert method " should {
		"do nothing if storage/stream are not defined" in {
			val conf = mock[Configuration]
			conf.getString("storage.type", None) returns None

			Context.configure(conf)
			Converter.convert("some") must_== None
		}
		"convert correctly on local file system" in {
			val conf = mock[Configuration]
			conf.getString("storage.type", None) returns Some("fs")
			conf.getString("fs.inbox", None) returns Some (base.concat ("/test/assets"))
			conf.getString("fs.outbox",None) returns Some (base.concat ("/target/test-out"))
			conf.getString("conversion.scale",None) returns Some("1.5")

			Context.configure(conf)
			Context.getStorage must_!= None
			Context.getStorage must_== Some (FileSystem)
			val result = Converter.convert("file1.pdf")

			result must_!=  None
			val list = Json.fromJson[List[String]](result.get)

			for (l <- list) {
				val file = new File(l)
				file.exists must_== true
				file.length > 0 must_== true
			}

			list.size > 0 must_== true
		}
	}
}
