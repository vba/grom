package tools.storage

import play.api.{Logger, Configuration}
import com.amazonaws.services.s3.{AmazonS3Client => S3}
import com.amazonaws.auth.{BasicAWSCredentials => Credentials}
import tools.security.Sha1DigestInputStream
import java.io.{FileInputStream, File, InputStream}
import com.amazonaws.services.s3.model.{AmazonS3Exception, PutObjectRequest, ObjectMetadata}

object Amazon extends Storage {

	private var bucket = ""
	private var prefix = ""
	private[storage] var client : Option[S3] = None
	
	def configure (c: Configuration) : Storage = {

		if (client.isDefined) return this
		Logger debug "Configuring amazon storage provider"

		val conf = (k:String) => c.getString ("amazon.".concat(k)).getOrElse("")

		client = Some (new S3 (new Credentials (conf ("access_key"), conf ("secret_key"))))
		bucket = conf ("bucket")
		prefix = conf ("prefix")

		this
	}

	def getStream (key : String) : Option[InputStream] = {
		if (!client.isDefined) return None;

		try {
			Some (client.get.getObject (bucket,key).getObjectContent);
		} catch {
			case e:RuntimeException => {
				Logger.error (e.getMessage, e)
				None
			}
			case _ => None
		}
	}

	def store (file: File) : String = {
		val key = hash(file, prefix).concat(".png")

		if (!(this has key)) {
			client.get.putObject(bucket, key, file)
			Logger.debug(key.concat(" is sent to amazon"))
		}
		key
	}

	private def has (key: String) : Boolean = {
		if (!client.isDefined) return false

		try { client.get.getObjectMetadata (bucket, key) != null }
		catch {
			case e: AmazonS3Exception => Logger.warn ("Key " + key + " : " + e.getMessage); return false
			case e: Throwable => Logger.error (e.getMessage, e); return false
		}
	}
}
