package tools.storage

import java.io.{File, InputStream}


trait Storage {

	def getStream (key : String) : Option[InputStream]
	def store (file: File) : String
}
