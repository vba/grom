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

	def store[T >: String] (file: File, parent:T) : T = {
		if (!file.exists()) return ""

		val key = "grom-" + tryToHash (file)
		val omd = new ObjectMetadata

		omd.setContentLength(file.length())
		omd.setContentType("image/png")

		store (file, omd, key, parent.toString)

		key
	}

	def has (key: String) : Boolean = {
		getMetaObject(key).isDefined
	}
	
	def getParents (key: String) : Option[String] = {

		val o = getMetaObject(key);
		if (o.isDefined) {
			Some (o.get.getUserMetadata.getOrElse("parents",""))
		}
		else {
			None
		}
	}
	
	private def getMetaObject (key: String) : Option[ObjectMetadata] = {
		if (!client.isDefined) return None

		try {
			val omd = client.get.getObjectMetadata (bucket, key)
			if (omd == null) return None
			else return Some (omd)
		}
		catch {
			case e: AmazonS3Exception => Logger.warn ("Key " + key + " : " + e.getMessage)
			case e: Throwable => Logger.error (e.getMessage, e)
		}
		None

	}

	private def store (file: File, omd: ObjectMetadata, key: => String, parent: String) {

		val parents = this getParents key
		
		if (parents.isDefined && !parents.get.contains (parent) ) {
			omd.setUserMetadata (Map("parents" -> (parents.get+"|"+parent)))
//			client.get.deleteObject(bucket,key);
		}
		else if (parents.isDefined) {
			return
		}
		else {
			omd.setUserMetadata (Map("parents" -> parent))
		}

		val is = new FileInputStream (file)
		client.get.putObject (bucket, key, is, omd)
		Logger debug key.concat(" is sent to amazon")
		is.close()
	}

	def getAccess = access
	def getSecret = secret
}
