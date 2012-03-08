package tools.storage

import java.io.{FileInputStream, File, InputStream}
import tools.security.{Sha1DigestInputStream => SDIStream}


trait Storage {

	protected val mimes = Seq ("application/pdf")

	def getStream (key : String, accept : Option[Seq[String]] = None) : Option[InputStream]
	def store (page: Int, file: File) : String
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

	def getMimes = mimes

	
}
