import play.api.{Logger, Configuration, Application, GlobalSettings}
import tools.storage.{Storage, Amazon}

object Global extends GlobalSettings {

	private val storageProviders = Map[String,Option[{def configure(c: Configuration): Storage}]] {
		"amazon" -> Some(Amazon)
	}
	
	private var storage: Option[Storage] = None

	override def onStart(app: Application) {
		val st = configuration.getString("storage.type").getOrElse(" EMPTY STORAGE TYPE ")
		val provider = storageProviders.getOrElse(st, None)

		if (!provider.isDefined) {
			Logger.warn("No storage provider defined for " concat st)
			return
		}

		storage = Some (provider.get configure configuration)
	}

	def getStorage = storage
}
