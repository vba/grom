package controllers

import play.api._
import libs.concurrent.Akka
import libs.json.{JsValue, Json}
import play.api.mvc._
import play.libs.Akka._
import play.api.Play.current

import tools.Context
import play.Configuration
import org.icepdf.core.util.GraphicsRenderingHints
import org.icepdf.core.pobjects.{Page, Document}
import javax.imageio.ImageIO
import java.awt.image.BufferedImage
import java.io.{FileInputStream, ByteArrayInputStream, ByteArrayOutputStream, File}
import java.awt.Image

object Converter extends Controller {

	private[controllers] var makeDocument = new Document
	private[controllers] var context = Context
	private[controllers] var toFile = (i:BufferedImage,f:File) => ImageIO.write (i, "png", f)

	private[controllers] def convert (id:String): Option[JsValue] = {
		if (!context.getStorage.isDefined) {
			Logger warn "No storage defined"
			return None
		}

		val stream = context.getStorage.get.getStream(id)

		if (!stream.isDefined) {
			Logger warn "No resource found for ".concat (id).concat (" key")
			return None
		}

		val document = makeDocument
		val rh = GraphicsRenderingHints.SCREEN
		val pb = Page.BOUNDARY_CROPBOX
		val cs = context.conversionScale
		var l = List.empty[String]

		document.setInputStream (stream.get, File.createTempFile("grom","grom").getCanonicalPath)

		for (i <- 0 to document.getNumberOfPages - 1) {
			val image = document.getPageImage (i, rh, pb, 0f, cs).asInstanceOf[BufferedImage]
			val tmp = File.createTempFile("grom-",".png")
			
			toFile (image, tmp)
			l = context.getStorage.get.store (tmp) :: l
		}

		Some (Json toJson l)
	}

	def pages (id:String) = Action {
		val promise = Akka.future {convert (id)}
		Async {
			promise.map { json =>
				if (json.isDefined) {
					//ResponseHeader (200, Map(CONTENT_TYPE -> "application/json"))
					Ok (json.get)
				} else {
					NotFound
				}
			}
		}
	}

}
