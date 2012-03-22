package controllers

import play.api.mvc._

import play.api.libs.json.Json
import io.Source
import java.util.Scanner
import play.api.libs.iteratee.Enumerator
import tools.extractors.types.Mime
import collection.immutable.Set
import tools.dto.{OfficeKey, PdfKey}
import play.api.http.Status
import tools.{Configurable, Context}
import tools.extractors.{PdfToPng, Extractable}

object Converter extends Controller {

	private[controllers] var context: Configurable = Context

	def tryExtractPages (id:String, mime:String) = Action {
		extractPages (id, mime)
	}

	def getPages(id: String, mime:String) = Action {
		pages (id, mime)
	}

	private[controllers] def pages[T <: String](id:T, mime:T): Result = {
		val key = id+PdfToPng.metaSuffix
		val exists = context.getStorage.get has key

		if (!exists) {
			toStatus (id, mime, NotFound)
		} else {
			val stream = context.getStorage.get getStream key
			SimpleResult (
				header = ResponseHeader(200, Map(CONTENT_TYPE -> "application/json")),
				body = Enumerator (
					try {new Scanner(stream.get).useDelimiter("\\A").next()}
					catch {case _ => "null"}
				)
			)
		}
	}
	private[controllers] def toStatus[T <: Status] (id:String, mime:String, success:T = Ok): T = {
		mime match {
			case it if Mime.pdf contains mime => context.getKeysToProcess add PdfKey (id); success
			case it if Mime.office contains mime => context.getKeysToProcess add OfficeKey (id); success
			case _ => UnsupportedMediaType.asInstanceOf[T]
		}
	}
	private[controllers] def extractPages[T <: String](id:T, mime:T) = toStatus(id,mime)
}
