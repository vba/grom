package controllers

import play.api.mvc._

import tools.Context
import tools.extractors.PdfToPng
import play.api.libs.json.Json
import io.Source
import java.util.Scanner
import play.api.libs.iteratee.Enumerator
import tools.dto.PdfKey

object Converter extends Controller {

	def tryExtractPages (id:String) = Action {
		Context.keysToProcess add PdfKey (id)
		Ok
	}

	def getPages(id: String) = Action {

		val key = id+PdfToPng.metaSuffix
		val exists = Context.getStorage.get has key

		if (!exists) {
			Context.keysToProcess add PdfKey (id)
			NotFound
		} else {
			val stream = Context.getStorage.get getStream key
			SimpleResult (
				header = ResponseHeader(200, Map(CONTENT_TYPE -> "application/json")),
				body = Enumerator (
					try {new Scanner(stream.get).useDelimiter("\\A").next()}
					catch {case _ => "null"}
				)
			)
		}
	}
}
