package tools.storage

import tools.security.{Sha1DigestInputStream => SDIStream}
import java.io.{FileOutputStream, FileInputStream, File, InputStream}
import play.api.Logger
import collection.Map


trait Storage {

	protected val mimes = Seq ("application/pdf","application/octet-stream")

	def getStream (key : String, accept : Option[Seq[String]] = None) : Option[InputStream]
	def store[T >: String] (file: File, parent:T) : T
	def storeMeta (key:String, content: String)
	def has (key: String) : Boolean
	def hash (file: File, prefix: String = "na"): String = {

		val is = new FileInputStream(file)
		val sha1 = SDIStream.make(is)
		val key = prefix.concat("-page-").concat(sha1.getHash)
		
		is.close()
		sha1.close()
		key
	}
	
	def toTempFile (in:InputStream) : File = {
		val file = File.createTempFile("grom-", ".tmp")
		val out = new FileOutputStream(file)

		var read = 0;
		val portion = new Array[Byte](1024)

		while ({read = in.read(portion); read != -1}) {
			out.write (portion, 0, read)
		}

		in.close()
		out.flush()
		out.close()

		file
	}

	def getMimes = mimes

	
}
