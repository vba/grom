package tools.extractors

import play.api.Logger
import tools.Context
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import org.icepdf.core.util.GraphicsRenderingHints
import org.icepdf.core.pobjects.{Page, Document}
import java.io.{InputStream, File}
import play.api.libs.json.Json
import tools.dto.{Png, Meta}
import collection.immutable.{Map, List}

object PdfToPng {

	val metaSuffix = "-meta.json"
	private[extractors] var context = Context
	private[extractors] var makeDocument: () => Document = () => new Document
	private[extractors] var toFile = (i:BufferedImage, f:File) => ImageIO.write (i, "png", f)

	private def preExtract (id: String) : (Boolean, Option[InputStream]) = {
		if (!context.getStorage.isDefined) {
			Logger warn "No storage defined, stoping"
			return (false,None)
		}

		val storage = context.getStorage.get
		val stream = storage.getStream (id, Some(storage.getMimes))

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

	def extract (id:String): Option[Meta] = {
		val (follow, stream) = preExtract(id)

		if (!follow) return None

		val document = makeDocument()
		val rh = GraphicsRenderingHints.SCREEN
		val pb = Page.BOUNDARY_CROPBOX
		val cs = context.conversionScale
		var l = List.empty[Png]
		var result: Option[Meta] = None

		try {
			document.setInputStream (stream.get, File.createTempFile("grom","grom").getCanonicalPath)
			val pages = document.getNumberOfPages
		
			Logger debug  "File " + id + " has " + pages + " pages"
		
			for (i <- 0 to pages - 1) {
				val image = document.getPageImage (i, rh, pb, 0f, cs).asInstanceOf[BufferedImage]
				val tmp = File.createTempFile("grom-",".png")
				val status = {if (i == (pages-1)) Meta.Done else Meta.InProgress}
		
				toFile (image, tmp)
				val key = context.getStorage.get.store (tmp, id)
				l = Png (i+1, key, "") :: l
				result = Some (putMeta (id, Meta (l.reverse, status)))
				image.flush()
			} 
		} catch {
			case e: Throwable => Logger.error ("Stopping processing " + e.getMessage, e)
		}

		stream.get.close()
		document.dispose()

		Logger debug  "File " + id + " is done"
		
		result
	}

	private def putMeta (key: String, m: Meta): Meta = {
		val json = m.toJson
		Context.getStorage.get.storeMeta (key+metaSuffix, Json.stringify(json))
		m
	}
}


