package tools.dto

import play.api.libs.json.{JsArray, JsObject, Json, JsValue}
import collection.{Seq}


object Meta {
	val InProgress = "in_progress"
	val Done = "done"
}

case class Meta(pages: List[Png], status: String = Meta.InProgress) {
	def toJson: JsValue = {
		val time = System.currentTimeMillis() / 1000L
		JsObject {
			Seq (
				("pages", pagesToJson),
				("status", Json toJson status),
				("time", Json toJson time)
			)
		}
	}

	private[dto] def pagesToJson : JsArray = {
		var list = List.empty[JsValue]
		for (page <- pages) {
			list = page.toJson :: list
		}
		JsArray (list.reverse)
	}
}

