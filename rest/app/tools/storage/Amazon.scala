package tools.storage

import play.api.{Logger, Configuration}
import com.amazonaws.services.s3.{AmazonS3Client => S3}
import com.amazonaws.auth.{BasicAWSCredentials => Credentials}
import com.amazonaws.services.s3.model.{AmazonS3Exception, ObjectMetadata}
import java.io._
import scala.collection.JavaConversions._
import collection.{Seq, Map, JavaConversions}


object Amazon extends Storage {

	private var access = ""
	private var secret = ""
	private[storage] var bucket = ""
	private[storage] var prefix = ""
	private[storage] var client : Option[S3] = None
	private[storage] var tryToHash = (f:File) => hash(f, prefix).concat(".png")

	def configure (c: Configuration) : Storage = {

		if (client.isDefined) return this
		Logger debug "Configuring amazon storage provider"

		val conf = (k:String) => c.getString ("amazon.".concat(k), None).getOrElse("")

		bucket = conf ("bucket")
		prefix = conf ("prefix")
		access = conf ("access_key")
		secret = conf ("secret_key")

		client = Some (new S3 (new Credentials (access, secret)))

		this
	}

	def storeMeta(key: String, file: File) {
		//store(file, key)
	}

	def storeMeta(key: String, content: String) {
		val meta = new ObjectMetadata
		val bytes = content.getBytes

		meta setContentEncoding "utf-8"
		meta setContentType "application/json"
		meta setContentLength bytes.length

		val is = new ByteArrayInputStream (bytes)

		client.get.putObject (bucket, key, is, meta)
		
		is.close()
	}

	def getStream (key : String, accept : Option[Seq[String]] = None) : Option[InputStream] = {
		if (!client.isDefined) return None;

		try {
			val amazonEntry = client.get.getObject (bucket,key)
			val mime = amazonEntry.getObjectMetadata.getContentType

			if (accept.getOrElse(Seq(mime)).contains(mime)) {
				Some (amazonEntry.getObjectContent)
			} else {
				Logger warn mime+ " is not supported by processing"
				None
			}
		} catch {
			case e:RuntimeException => {
				Logger.error (e.getMessage, e)
				None
			}
			case _ => None
		}
	}

	def store[T >: String] (file: File, ai: Map[T, T] = Map.empty[T, T]): T = {
		if (!file.exists()) return ""

		val key = "grom-" + tryToHash (file)
		val omd = new ObjectMetadata

		omd.setContentLength(file.length())
		omd.setContentType("image/png")

		if (ai.isEmpty)
			omd.setUserMetadata (ai.asInstanceOf[Map[String, String]])

		store (file, omd, key)

		key
	}

//	def store (page : Int, file: File) : String = {
//		val key = page +"-amazon-"+ tryToHash (file)
//		store (file, key)
//		key
//	}
	
	def has (key: String) : Boolean = {
		if (!client.isDefined) return false

		try { client.get.getObjectMetadata (bucket, key) != null }
		catch {
			case e: AmazonS3Exception => Logger.warn ("Key " + key + " : " + e.getMessage); return false
			case e: Throwable => Logger.error (e.getMessage, e); return false
		}
	}

	private def store (file: File, omd: ObjectMetadata, key: => String ) {
		if (this has key) return

		val is = new FileInputStream (file)
		client.get.putObject (bucket, key, is, omd)
		Logger debug key.concat(" is sent to amazon")
		is.close()

		return
	}

	def getAccess = access
	def getSecret = secret
}
