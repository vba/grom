package tools.extractors

import play.api.libs.json.{JsObject, Json, JsValue}


object Meta {
	val InProgress = "in_progress"
	val Done = "done"
}

case class Meta (pages: List[String], status: String = Meta.InProgress) {
	def toJson: JsValue = {
		val time = System.currentTimeMillis() / 1000L
		JsObject { Seq (
			("pages", Json toJson pages),
			("status", Json toJson status),
			("time", Json toJson time)
		)}
	}
}

