package tools

import play.api.{Logger, Configuration}
import storage.{FileSystem, Amazon, Storage}


object Context {

	private val storageProviders = Map[String,Option[{def configure(c: Configuration): Storage}]] (
		"amazon" -> Some(Amazon),
		"fs" -> Some(FileSystem)
	)

	private var storage: Option[Storage] = None
	private var config : Option[Configuration] = None

	def configure (conf : Configuration) {

		config = Some (conf)

		val st = conf.getString("storage.type", None).getOrElse(" EMPTY STORAGE TYPE ")
		val provider = storageProviders.getOrElse(st, None)

		if (!provider.isDefined) {
			Logger.warn("No storage provider defined for " concat st)
			return
		}
		
		storage = Some (provider.get configure conf)
	}

	private def getConfig[T] (k:String,df:String="") : T = {
		config.get.getString ("conversion.".concat(k), None).getOrElse(df).asInstanceOf[T]
	}
	def conversionScale = getConfig[String]("scale","2.5").toFloat
	def getStorage = storage
	def getConfig = config
}