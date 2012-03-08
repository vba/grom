package tools.storage

import java.io.{FileInputStream, File, InputStream}
import tools.security.{Sha1DigestInputStream => SDIStream}


trait Storage {

	def getStream (key : String) : Option[InputStream]
	def store (page: Int, file: File) : String
	def storeMeta (key:String, file: File)
	def has (key: String) : Boolean
	def hash (file: File, prefix: String = "na"): String = {

		val is = new FileInputStream(file)
		val sha1 = SDIStream.make(is)
		val key = prefix.concat("-page-").concat(sha1.getHash)
		
		is.close()
		sha1.close()
		key
	}
}
