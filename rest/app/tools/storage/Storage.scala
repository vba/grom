package tools.storage

import java.io.{FileInputStream, File, InputStream}
import tools.security.{Sha1DigestInputStream => SDIStream}


trait Storage {

	def getStream (key : String) : Option[InputStream]
	def store (file: File) : String
	def hash (file: File, prefix: String = ""): (String, SDIStream) = {
		val is = new FileInputStream(file)
		val sha1 = SDIStream.make(is)
		val key = prefix.concat("-page-").concat(sha1.getSha1)
		
		is.close()
		(key, sha1)
	}
}
