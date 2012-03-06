import play.api.{Logger, Configuration, Application, GlobalSettings}
import tools.Context
import tools.storage.{Storage, Amazon}

object Global extends GlobalSettings {


	override def onStart(app: Application) {
		Context configure app.configuration
	}
}
