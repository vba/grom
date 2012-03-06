package tools.storage

import tools.Context
import java.io.{FileOutputStream, FileInputStream, InputStream, File}
import play.api.{Configuration, Logger}


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
		val (key,sha1) = hash(file, prefix)
		write (sha1,key);
	}

	private def getPath (sp: String, key: String = inboxKey) : String = {
		Context
			.getConfig.get
			.getString(key)
			.getOrElse("~/Temp")
			.concat("/")
			.concat(sp)
	}
	
	private def combineInbox (sp: String): String = inbox.getOrElse(tmp).getCanonicalPath.concat("/").concat(sp)
	private def combineOutbox (sp: String): String = outbox.getOrElse(tmp).getCanonicalPath.concat("/").concat(sp)

	private def write (is:InputStream, key:String) : String = {
		val file = new File (combineOutbox(key).concat(".png"))
		if (file.exists()) file.delete()

		var read = 0
		val out = new FileOutputStream (file)
		val portion = new Array[Byte](1024)

		while ((read = is.read(portion)) != -1) {
			out.write(portion, 0, read);
		}

		is.close()
		out.flush()
		out.close()

		file.getCanonicalPath
	}
}
