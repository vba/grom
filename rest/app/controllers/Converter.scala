package controllers

import play.api._
import libs.concurrent.Akka
import libs.json.{JsValue, Json}
import play.api.mvc._
import play.libs.Akka._
import play.api.Play.current

import org.icepdf.core.pobjects.Document
import tools.Context
import java.io.File

object Converter extends Controller {

/*
	public static void uploadFile (Blob blob) {

		final String prefix = prepareUploadFolders();
		Document document = new Document();
		try {
			document.setInputStream(blob.get(), blob.getFile().getAbsolutePath());
		} catch (Exception ex) {
			Logger.error(ex.getMessage(),ex);
		}

		float scale = 2.5f;
		float rotation = 0f;

		for (int i = 0; i < document.getNumberOfPages(); i++) {
			BufferedImage image = (BufferedImage) document
				.getPageImage(i,
				              GraphicsRenderingHints.SCREEN,
				              Page.BOUNDARY_CROPBOX,
				              rotation,
				              scale
				);
			try {
				Logger.debug("\t capturing page " + i);
				File file = new File(prefix+"page_" + i + ".png");
				ImageIO.write(image, "png", file);

			} catch (IOException e) {
				e.printStackTrace();
			}
			image.flush();
		}
		document.dispose();
		list();
	}

*/

	private def convert (id:String): Option[JsValue] = {
		if (!Context.getStorage.isDefined) {
			Logger warn "No storage defined"
			return None
		}

		val stream = Context.getStorage.get.getStream(id)

		if (!stream.isDefined) {
			Logger warn "No resource found for ".concat (id).concat (" key")
			return None
		}

		val document = new Document
		document.setInputStream (stream.get, File.createTempFile("grom","grom").getCanonicalPath)

		Some(Json.toJson(""))
	}

	def test1 (id:String) = Action {
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
