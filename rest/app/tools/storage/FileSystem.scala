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
			val dir = new File (c.getString(k) getOrElse "~/Temp/".concat(k.replace("fs.","")))
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

	def getStream (key: String) : Option[InputStream] = {
		try {
			Some(new FileInputStream(combineInbox (key)))
		} catch {
			case e:Throwable => Logger.error(e.getMessage, e); None
		}
	}

	def store (file: File) : String = {
		val key = hash(file, prefix)
		write (file, key)
	}
	
	private def combineInbox (sp: String): String = inbox.getOrElse(tmp).getCanonicalPath.concat("/").concat(sp)
	private def combineOutbox (sp: String): String = outbox.getOrElse(tmp).getCanonicalPath.concat("/").concat(sp)

	private def write (in:File, key:String) : String = {
		val out = new File (combineOutbox(key).concat(".png"))
		Files.copyFile (in, out, false, true)
		out.getCanonicalPath
	}
}
