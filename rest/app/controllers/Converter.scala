package controllers

import play.api.mvc._

import tools.Context

object Converter extends Controller {

	def pages (id:String) = Action {
		Context.keysToProcess add id
		Ok
	}

}
