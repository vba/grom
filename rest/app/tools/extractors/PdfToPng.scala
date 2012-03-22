package tools.extractors

import play.api.Logger
import tools.Context
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import org.icepdf.core.util.GraphicsRenderingHints
import org.icepdf.core.pobjects.{Page, Document}
import play.api.libs.json.Json
import tools.dto.{Png, Meta}
import collection.immutable.{Map, List}
import java.io.{FileInputStream, InputStream, File}

object PdfToPng extends Extractable {

	private[extractors] var makeDocument: () => Document = () => new Document
	private[extractors] var toFile = (i:BufferedImage, f:File) => ImageIO.write (i, "png", f)

	override def extract (id: String): Option[Meta] = {

		val (follow, stream) = onBeforeExtract(id, Some(id+metaSuffix))
		if (!follow) return None

		val result: Option[Meta] = toPdf(id, stream.get)
		Logger debug  "File " + id + " is done"
		result
	}

	private[extractors] def toPdf (id: String, file: File): Option[Meta] = {
		val is = new FileInputStream(file);
		toPdf (id, is)
	}

	private[extractors] def toPdf (id: String, is: InputStream): Option[Meta] = {

		val document = makeDocument()
		val rh = GraphicsRenderingHints.SCREEN
		val pb = Page.BOUNDARY_CROPBOX
		val cs = context.conversionScale
		var l = List.empty[Png]
		var result: Option[Meta] = None

		try {
			document.setInputStream(is, File.createTempFile("grom", "grom").getCanonicalPath)
			val pages = document.getNumberOfPages

			Logger debug "File " + id + " has " + pages + " pages"

			for (i <- 0 to pages - 1) {
				val image = document.getPageImage(i, rh, pb, 0f, cs).asInstanceOf[BufferedImage]
				val tmp = File.createTempFile("grom-", ".png")
				val status = {if (i == (pages - 1)) Meta.Done else Meta.InProgress}

				toFile(image, tmp)
				val key = context.getStorage.get.store(tmp, id)
				l = Png(i + 1, key, "") :: l
				result = Some(putMeta(id, Meta(l.reverse, status)))
				image.flush()
			}
		} catch {
			case e: Throwable => Logger.error("Stopping processing " + e.getMessage, e)
		}

		is.close()
		document.dispose()
		result
	}

	private def putMeta (key: String, m: Meta): Meta = {
		val json = m.toJson
		Context.getStorage.get.storeMeta (key+metaSuffix, Json.stringify(json))
		m
	}
}


