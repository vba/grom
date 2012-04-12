package controllers

import play.api._
import libs.iteratee.Enumerator
import play.api.mvc._
import play.api.Play.current
import tools.{Context, Configurable}

object Configuration extends Controller{

	private[controllers] var context: Configurable = Context
	private[controllers] var conf = (s:String) => s + "\t" + Play.configuration.getString(s).getOrElse("[NA]") + "\n"

	def show = Action {
		if (context.configIsVisible) showConfigStream
		else Forbidden
	}

	private[controllers] def showConfigStream = {
		val body = conf ("storage.type") +
			conf ("amazon.access_key") +
			conf ("amazon.secret_key") +
			conf ("amazon.bucket") +
			conf ("amazon.prefix") +
			conf ("fs.inbox") +
			conf ("fs.outbox") +
			conf ("conversion.scale") +
			conf ("conversion.preview_scale") +
			conf ("conversion.libre_office") +
			conf ("conversion.allow_config_display")

		SimpleResult (
			header = ResponseHeader(200, Map(CONTENT_TYPE -> "text/plain")),
			body = Enumerator(body)
		)
	}
}
