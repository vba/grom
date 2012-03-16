package tools.dto

import play.api.libs.json.{Json, JsObject, JsValue}


case class Png (page: Int, key: String, previewKey: String) {
	def toJson: JsValue = {
		val time = System.currentTimeMillis() / 1000L
		JsObject {
			Seq (
				("page", Json toJson page),
				("key", Json toJson key),
				("previewKey", Json toJson previewKey),
				("time", Json toJson time)
			)
		}
	}
}
