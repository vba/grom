package tools

import storage.{Amazon, Storage}
import play.api.{Logger, Configuration}


object Context {

	private val storageProviders = Map[String,Option[{def configure(c: Configuration): Storage}]] {
		"amazon" -> Some(Amazon)
	}

	private var storage: Option[Storage] = None

	def configure (conf : Configuration) {

		val st = conf.getString("storage.type").getOrElse(" EMPTY STORAGE TYPE ")
		val provider = storageProviders.getOrElse(st, None)

		if (!provider.isDefined) {
			Logger.warn("No storage provider defined for " concat st)
			return
		}

		storage = Some (provider.get configure conf)
	}

	def getStorage = storage
}
