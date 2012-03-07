package tools.extractors

import play.api.{Logger, Configuration}
import tools.storage.{FileSystem, Amazon, Storage}
import scala.collection.mutable.{Set, SynchronizedSet, HashSet}
import tools.Context
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import org.icepdf.core.util.GraphicsRenderingHints
import org.icepdf.core.pobjects.{Page, Document}
import play.api.libs.json.{Json, JsValue}
import io.Source
import play.api.libs.Files
import java.io.{InputStream, ByteArrayInputStream, File}

object PdfToPng {

	val metaSuffix = "-meta.json"
	private[extractors] var makeDocument = new Document
	private[extractors] var context = Context
	private[extractors] var toFile = (i:BufferedImage, f:File) => ImageIO.write (i, "png", f)

	private def preExtract (id: String) : (Boolean, Option[InputStream]) = {
		if (!context.getStorage.isDefined) {
			Logger warn "No storage defined, stoping"
			return (false,None)
		}

		val stream = context.getStorage.get.getStream(id)

		if (!stream.isDefined) {
			Logger warn "No resource found for ".concat (id).concat (" key") + " , stoping"
			return (false,stream)
		}

		if (context.getStorage.get has id+metaSuffix) {
			Logger debug id + " is already processed, stoping"
			return (false,stream)
		}

		(true,stream)
	}

	def extract (id:String): Option[JsValue] = {
		val (follow, stream) = preExtract(id)

		if (!follow) return None

		val document = makeDocument
		val rh = GraphicsRenderingHints.SCREEN
		val pb = Page.BOUNDARY_CROPBOX
		val cs = context.conversionScale
		var l = List.empty[String]

		document.setInputStream (stream.get, File.createTempFile("grom","grom").getCanonicalPath)

		Logger debug  "File " + id + " has " + document.getNumberOfPages + " pages"
		for (i <- 0 to document.getNumberOfPages - 1) {
			val image = document.getPageImage (i, rh, pb, 0f, cs).asInstanceOf[BufferedImage]
			val tmp = File.createTempFile("grom-",".png")

			toFile (image, tmp)
			l = context.getStorage.get.store (tmp) :: l
		}

		Some (putMeta (id, l))
	}

	private def putMeta[T <: String] (key: T ,l: List[T]): JsValue = {
		val json = Json.toJson [List[String]] (l)
		val file = File.createTempFile("grom",".json");
		Files.writeFile (file, json.toString())
		Logger debug "Store meta to "+ key + metaSuffix
		Context.getStorage.get.storeMeta (key+metaSuffix, file)
		json
	}
}
