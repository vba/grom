package controllers

import play.api._
import play.api.mvc._
import play.api.Play.current

object Application extends Controller {

	def index = Action {
		if (Play.isProd)
			NotFound
		else
			Ok(views.html.index("Your new application is ready."))
	}

}