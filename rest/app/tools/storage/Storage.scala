package tools.storage

import java.io.InputStream

trait Storage {

	def getStream (key : String) : Option[InputStream]
}
