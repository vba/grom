package tools.extractors

import tools.dto.Meta
import java.io.{InputStream, File}

trait PdfCapable {
	def toPng (id: String, file: File): Option[Meta]
	def toPng (id: String, is: InputStream): Option[Meta]
}
