import java.util.concurrent.TimeUnit
import akka.util.Duration
import play.api.Play.current
import play.api.libs.concurrent.Akka
import play.api.{Application, GlobalSettings}
import tools.{Configurable, Context}

object Global extends GlobalSettings {

	private val sec1 = Duration(0, TimeUnit.SECONDS)
	private val sec2 = Duration(20, TimeUnit.SECONDS)

	private var context: Configurable = Context
	private var scheduler = () => Akka.system.scheduler

	override def onStart(app: Application) {

		context configure app.configuration
		context.startOffice ()

		scheduler().schedule (sec1, sec2) {
			context.processKeys()
		}
	}

	override def onStop(app: Application) {
		context.stopOffice()
	}
}
