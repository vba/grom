package tools

import dto.{Key, Meta}
import extractors.{PdfToPng}
import play.api.{Logger, Configuration}
import storage.{FileSystem, Amazon, Storage}

import play.api.libs.json.JsValue
import eu.medsea.mimeutil.MimeUtil
import play.api.Play.current
import play.api.{Play, Logger}
import collection.mutable.{Set, SynchronizedSet, HashSet}

trait Configurable {
	def configure (conf : Configuration)
	def getConfig: Option[Configuration]
	def getStorage: Option[Storage]
	def getKeysToProcess: Set[Key]
}

object Context extends Configurable {

	val keysToProcess = new HashSet[Key] with SynchronizedSet[Key]
	var isProd = () => Play.isProd
	var isDev = () => Play.isDev
	var isTest = () => Play.isTest

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

	def processKeys () {
		if (Context.keysToProcess.size == 0) return
		
		val key = Context.keysToProcess.head
		Context.keysToProcess.remove(key)
		Logger debug  "Process "+key
		
		if (key.extractor.isDefined) {
			key.extractor.get.extract(key.id)
		}
	}

	private def getConfig[T] (k:String,df:String="") : T = {
		config.get.getString ("conversion.".concat(k), None).getOrElse(df).asInstanceOf[T]
	}
	def conversionScale = getConfig[String]("scale","2.5").toFloat
	def previewScale = getConfig[String]("preview_scale","0.3").toFloat
	def getStorage = storage
	def getConfig = config
	def getKeysToProcess = keysToProcess
}