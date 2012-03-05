package tools.security

import java.io.InputStream
import java.security.{MessageDigest, DigestInputStream}

case class Sha1DigestInputStream private[security] (inputStream: InputStream, messageDigest: MessageDigest)
	extends DigestInputStream (inputStream, messageDigest) {

	def getSha1: String = {
		val hash = getMessageDigest.digest
		val sb = new StringBuilder
		for (aHash <- hash) {
			val hex = Integer.toHexString(0xFF & aHash)
			if (hex.length == 1) {
				sb.append('0')
			}
			sb.append(hex)
		}
		sb.toString()
	}
}

object Sha1DigestInputStream {
	def make (stream: InputStream) : Sha1DigestInputStream = {
		Sha1DigestInputStream (stream, MessageDigest.getInstance("SHA-1"))
	}
}
