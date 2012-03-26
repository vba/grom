package controllers

import play.api.mvc._

import java.util.Scanner
import play.api.libs.iteratee.Enumerator
import tools.extractors.types.Mime
import play.api.http.Status
import tools.{Configurable, Context}
import tools.extractors.PdfToPng
import tools.dto.{Png, Meta, OfficeKey, PdfKey}
import play.api.libs.json.Json

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
		val header = ResponseHeader(200, Map(CONTENT_TYPE -> "application/json"))

		if (!exists) {
			val json = Meta (List.empty[Png], Meta.Await, 0).toJson
			val status = SimpleResult(
				header = header,
				body = Enumerator (Json stringify json)
			)
			toStatus (id, mime, status)
		} else {
			val stream = context.getStorage.get getStream key
			SimpleResult (
				header = header,
				body = Enumerator (
					try {new Scanner(stream.get).useDelimiter("\\A").next()}
					catch {case _ => "null"}
				)
			)
		}
	}

	type T1 = SimpleResult[_]

	private[controllers] def toStatus (id:String, mime:String, success: => T1 = Ok): T1 = {
		mime match {
			case it if Mime.pdf contains mime => context.getKeysToProcess add PdfKey (id); success
			case it if Mime.office contains mime => context.getKeysToProcess add OfficeKey (id); success
			case _ => UnsupportedMediaType
		}
	}
	private[controllers] def extractPages[T <: String](id:T, mime:T) = toStatus(id,mime)
}
