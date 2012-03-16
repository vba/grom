package tools

import dto.Meta
import extractors.{PdfToPng}
import play.api.{Logger, Configuration}
import storage.{FileSystem, Amazon, Storage}
import scala.collection.mutable.{SynchronizedSet, HashSet}
import collection.immutable.Set
import play.api.libs.json.JsValue
import eu.medsea.mimeutil.MimeUtil


object Context {

	val keysToProcess = new HashSet[String] with SynchronizedSet[String]

	private val storageProviders = Map[String,Option[{def configure(c: Configuration): Storage}]] (
		"amazon" -> Some(Amazon),
		"fs" -> Some(FileSystem)
	)

	val extractors = Set[Option[{def extract (id:String): Option[Meta]}]] (
		Some(PdfToPng)
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
		
		for (extractor <- Context.extractors if extractor.isDefined) {
			extractor.get.extract(key)
		}
	}

	private def getConfig[T] (k:String,df:String="") : T = {
		config.get.getString ("conversion.".concat(k), None).getOrElse(df).asInstanceOf[T]
	}
	def conversionScale = getConfig[String]("scale","2.5").toFloat
	def previewScale = getConfig[String]("preview_scale","0.3").toFloat
	def getStorage = storage
	def getConfig = config
}