package tools

import dto.Key
import play.api.Configuration
import storage.{FileSystem, Amazon, Storage}

import play.api.Play.current
import play.api.{Play, Logger}
import collection.mutable.{Set, SynchronizedSet, HashSet}
import org.artofsolving.jodconverter.office.{OfficeConnectionProtocol, DefaultOfficeManagerConfiguration, OfficeManager}

trait Configurable {
	def configure (conf : Configuration)
	def getConfig: Option[Configuration]
	def getStorage: Option[Storage]
	def getOffice: Option[OfficeManager]
	def getKeysToProcess: Set[Key]
	def startOffice ()
	def stopOffice ()
	def processKeys ()
	def conversionScale: Float
	def previewScale: Float
}

object Context extends Configurable {

	private[tools] val keysToProcess = new HashSet[Key] with SynchronizedSet[Key]
	private[tools]  val keysInProcess = new HashSet[Key] with SynchronizedSet[Key]
	var isProd = () => Play.isProd
	var isDev = () => Play.isDev
	var isTest = () => Play.isTest

	private val storageProviders = Map[String,Option[{def configure(c: Configuration): Storage}]] (
		"amazon" -> Some(Amazon),
		"fs" -> Some(FileSystem)
	)
	private[tools] var office: Option[OfficeManager] = None
	private var storage: Option[Storage] = None
	private var config: Option[Configuration] = None

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

	def startOffice () {
		Logger debug "Starting office component"
		val om = new DefaultOfficeManagerConfiguration ()
			.setOfficeHome (getConfigByKey ("libre_office"))
			.setConnectionProtocol (OfficeConnectionProtocol.SOCKET)
			.buildOfficeManager ()

		om.start()
		office = Some (om)
	}

	def stopOffice () {
		if (!office.isDefined) {
			Logger warn "Office component is not defined nothing to stop"
			return
		}
		
		Logger debug "Stopping office component"
		office.get.stop()
	}

	def processKeys () {
		if (keysToProcess.size == 0) return
		
		val key = keysToProcess.head

		if (keysInProcess contains key) return
		else keysInProcess add key

		Logger debug "Process "+key
		
		if (key.extractor.isDefined) {
			key.extractor.get.extract(key.id)
		}

		keysInProcess remove key
		keysToProcess remove key

		Logger debug "Ending "+key
	}

	private def getConfigByKey[T <: String] (k:T, df:T=""): T = {
		config.get.getString ("conversion.".concat(k), None).getOrElse(df).asInstanceOf[T]
	}

	def conversionScale = getConfigByKey("scale","2.5").toFloat
	def previewScale = getConfigByKey("preview_scale","0.3").toFloat
	def getStorage = storage
	def getConfig = config
	def getKeysToProcess = keysToProcess
	def getOffice = office
}