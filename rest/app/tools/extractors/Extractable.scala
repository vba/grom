package tools.extractors

import tools.Context
import java.io.InputStream
import play.api.Logger
import tools.dto.Meta
import collection.mutable.HashSet
import collection.mutable.SynchronizedSet

trait Extractable {

	val banned = new HashSet[String] with SynchronizedSet[String]
	protected[extractors] var context = Context
	protected def onBeforeExtract[T <: String] (id: T, meta: Option[T] = None)
		: (Boolean, Option[InputStream]) = {

		if (!context.getStorage.isDefined) {
			Logger warn "No storage defined, stoping"
			return (false,None)
		}

		val storage = context.getStorage.get
		val stream = storage.getStream (id, Some(storage.getMimes))

		if (!stream.isDefined) {
			Logger warn "No resource found for ".concat (id).concat (" key") + " , stoping"
			return (false,stream)
		}

		if (meta.isDefined && context.getStorage.get.has (meta.get)) {
			Logger debug id + " is already processed, stoping"
			return (false,stream)
		}

		(true, stream)
	}

	def extract (id:String): Option[Meta] = None
}
