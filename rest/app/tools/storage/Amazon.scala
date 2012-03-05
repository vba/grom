package tools.storage

import play.api.{Logger, Configuration}
import com.amazonaws.services.s3.{AmazonS3Client => S3}
import com.amazonaws.auth.{BasicAWSCredentials => Credentials}
import java.io.InputStream

object Amazon extends Storage {

	private var bucket = ""
	private var prefix = ""
	private var client : Option[S3] = None
	
	def configure (c: Configuration) : Storage = {

		if (client.isDefined) return this
		Logger debug "Configuring amazon storage provider"

		val conf = (k:String) => c.getString ("amazon.".concat(k)).getOrElse("")

		client = Some (new S3 (new Credentials (conf ("access_key"), conf ("secret_key"))))
		bucket = conf ("access_key")
		prefix = conf ("access_key")

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
}
