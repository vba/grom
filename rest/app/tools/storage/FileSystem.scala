package tools.storage

import tools.Context
import play.api.Logger
import java.io.{FileInputStream, InputStream, File}


object FileSystem extends Storage {

	private val prefix = "fs"
	private val inbox = "fs.inbox"
	private val outbox = "fs.outbox"

	def getStream (key: String) : Option[InputStream] = {
		try {
			Some(new FileInputStream(getPath(key)))
		} catch {
			case e:Throwable => Logger.error(e.getMessage, e); None
		}
	}

	def store (file: File) : String = {
		val (key,sha1) = hash(file, prefix)
		
		key
	}

	private def getPath (sp: String, key: String = inbox) : String = {
		Context
			.getConfig.get
			.getString(key)
			.getOrElse("~/Temp")
			.concat("/")
			.concat(sp)
	}

	private def write (is:InputStream, key:String) : String = {
		val file = new File (getPath (key,outbox).concat(".png"))

		if (!file.exists) {
			// @TraineeCode possible collisions refactor after
			if (!file.getParentFile.exists()) file.mkdirs
		}
		file.getCanonicalPath
	}
}
