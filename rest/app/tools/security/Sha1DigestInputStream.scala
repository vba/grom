package tools.security

import java.io.InputStream
import java.security.{MessageDigest, DigestInputStream}

case class Sha1DigestInputStream private[security] (inputStream: InputStream, messageDigest: MessageDigest)
	extends DigestInputStream (inputStream, messageDigest) {

	private var hash: Option[String] = None
	
	def getHash: String = {

		if (hash.isDefined) return hash.get

		val digest = getMessageDigest.digest
		val sb = new StringBuilder
		for (h <- digest) {
			val hex = Integer.toHexString(0xFF & h)
			if (hex.length == 1) {
				sb.append('0')
			}
			sb.append(hex)
		}
		hash = Some(sb.toString())
		hash.get
	}
}

object Sha1DigestInputStream {
	def make (stream: InputStream) : Sha1DigestInputStream = hashAndMake(stream)

	private def hashAndMake (is: InputStream) : Sha1DigestInputStream = {
		val sha1 = Sha1DigestInputStream (is, MessageDigest.getInstance("SHA-1"))
		val portion = new Array[Byte](1024)
		var read = 0;

		while ({read = sha1.read(portion); read != -1}) {
			sha1.messageDigest.update (portion, 0, read)
		}

		sha1
	}
}
