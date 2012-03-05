package controllers

import play.api._
import play.api.mvc._
import play.libs.Akka._

import org.icepdf.core.pobjects.Document

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

	def test1() = Action {
		AsyncResult
		Async {
			val document = new Document()
			Ok("In progress")
		}
		//Ok("In progress")
	}
}
