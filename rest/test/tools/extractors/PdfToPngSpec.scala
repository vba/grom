package tools.extractors

import org.specs2.mutable._
import org.mockito.Matchers._
import org.specs2.mock._
import tools.Context
import play.api.Configuration
import java.io.File
import tools.storage.FileSystem

class PdfToPngSpec extends Specification with Mockito {

	val base = new File(".").getCanonicalPath
	
	def delete(f:File) {
		if (f.isDirectory) for (file <- f.listFiles()) delete(file)
		f.delete()
	}

	delete (new File(base.concat("/target/test-out")))
	
	"Extract method " should {
		"do nothing if storage/stream are not defined" in {
			val conf = mock[Configuration]
			conf.getString("storage.type", None) returns None

			Context.configure(conf)
			PdfToPng.extract("some") must_== None
		}
		"extract correctly on local file system" in {
			val conf = mock[Configuration]

			conf.getString("storage.type", None) returns Some("fs")
			conf.getString("fs.inbox", None) returns Some(base.concat("/test/assets"))
			conf.getString("fs.outbox", None) returns Some(base.concat("/target/test-out"))
			conf.getString("conversion.scale", None) returns Some("1.5")

			Context.configure(conf)
			Context.getStorage must_!= None
			Context.getStorage must_== Some(FileSystem)
			val result = PdfToPng.extract("file1.pdf")

			result must_!= None
			val meta = result.get
			meta must_!= null

			for (l <- meta.pages) {
				val file = new File(l)
				file.exists must_== true
				file.length > 0 must_== true
			}

			meta.pages.size > 0 must_== true
		}
	}
}
