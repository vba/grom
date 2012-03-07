package tools.storage

import java.io.{FileInputStream, InputStream, File}
import play.api.{Configuration, Logger}
import play.api.libs.Files


object FileSystem extends Storage {

	private val prefix = "fs"
	private val inboxKey = "fs.inbox"
	private val outboxKey = "fs.outbox"

	private val tmp = new File (System getProperty "java.io.tmpdir")
	private var inbox: Option[File] = None
	private var outbox: Option[File] = None

	def configure (c: Configuration) : Storage = {

		val f1 : (String) => Option[File] = (k: String) => {
			val dir = new File (c.getString(k, None) getOrElse "~/Temp/".concat(k.replace("fs.","")))
			if (!dir.exists()) {
				// @TraineeCode possible collisions refactor after
				Logger debug  "Creating ".concat(dir.getCanonicalPath)
				dir.mkdirs()
			}
			Some (dir)
		}
		
		inbox = f1 (inboxKey)
		outbox = f1 (outboxKey)
		this
	}

	def storeMeta(key: String, file: File) {
		write (file, key, "")
	}

	def getStream (key: String) : Option[InputStream] = {
		try {
			Some(new FileInputStream(combineInbox (key)))
		} catch {
			case e:Throwable => Logger.error(e.getMessage, e); None
		}
	}

	def has (key: String) : Boolean = new File(combineOutbox(key)).exists()

	def store (file: File) : String = {
		val key = hash(file, prefix)
		write (file, key)
	}

	private def combineInbox (sp: String): String = inbox.getOrElse(tmp).getCanonicalPath.concat("/").concat(sp)
	private def combineOutbox (sp: String): String = outbox.getOrElse(tmp).getCanonicalPath.concat("/").concat(sp)

	private def write (in:File, key:String, suffix: String = ".png") : String = {
		val out = new File (combineOutbox(key).concat(suffix))
		Files.copyFile (in, out, false, true)
		out.getCanonicalPath
	}

}
