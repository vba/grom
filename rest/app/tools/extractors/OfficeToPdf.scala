package tools.extractors

import tools.dto.Meta
import org.artofsolving.jodconverter.{OfficeDocumentConverter => ODC}
import java.io.{FileOutputStream, File, InputStream}
import org.artofsolving.jodconverter.document.DocumentFormat
import play.api.{Logger, Play}

object OfficeToPdf extends Extractable {

	private [extractors] var makeConverter: () => ODC = () => new ODC (context.getOffice.get)
	private val format = new DocumentFormat ("pdf","pdf","application/pdf")


	override def extract (id: String): Option[Meta] = {

		val (follow, stream) = onBeforeExtract (id, Some (id+metaSuffix))
		if (!follow) return None

		val in = this toFile stream.get
		val out = tmpFile (".pdf")
		val converter = makeConverter ()

		try {
			converter.convert (in, out)
		} catch {
			case e:Throwable => Logger.error(e.getMessage, e)
			return None
		}

		PdfToPng.toPdf (id, out)

		None
	}

	private def toFile (is: InputStream): File = {
		val file = tmpFile ()
		val out = new FileOutputStream(file)
		val buffer = new Array[Byte](10240)
		var i = 0

		while ({i = is.read(buffer); i != -1}) {
			out.write (buffer, 0, i)
		}

		out.close()
		file
	}
}
