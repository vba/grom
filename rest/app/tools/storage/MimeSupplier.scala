package tools.storage

import java.io.{File, InputStream}
import eu.medsea.mimeutil.MimeUtil
import java.util.{Collection, ArrayList}


object MimeSupplier {
	
	private[storage] var byInputStream : (InputStream) => Option[String] = null
	private[storage] var byFile: (File) => Option[String] = null

	private[storage] def restoreDelegates () {
		byInputStream = (is: InputStream) => None
		byFile = (file: File)  => None
	}

	restoreDelegates()

	def supply (file: File) : Option[String] = {
		val mime = byFile(file);
		
		if (mime.isDefined) mime
		else getMime (MimeUtil.getMimeTypes(file))
	}
	
	def supply (is : InputStream) : Option[String] = {
		val mime = byInputStream (is);

		if (mime.isDefined) mime
		else getMime (MimeUtil.getMimeTypes(is))
	}

	private def getMime (c: => Collection[_]) : Option[String] = {
		try { Some(new ArrayList(c).get(0).toString) } catch { case _ => None }
	}
}
