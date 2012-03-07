import java.util.concurrent.TimeUnit
import akka.util.Duration
import play.api.Play.current
import play.api.libs.concurrent.Akka
import play.api.{Logger, Configuration, Application, GlobalSettings}
import tools.Context

object Global extends GlobalSettings {

	private val sec1 = Duration(0, TimeUnit.SECONDS)
	private val sec2 = Duration(20, TimeUnit.SECONDS)

	override def onStart(app: Application) {
		Context configure app.configuration

		Akka.system.scheduler.schedule (sec1, sec2) {
			Context.processKeys()
		}
	}
}
