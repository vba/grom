package tools.storage

import org.specs2.mutable._
import org.mockito.Matchers._
import org.specs2.mock._
import scala.collection.JavaConversions._
import com.amazonaws.services.s3.{AmazonS3Client => S3}
import com.amazonaws.AmazonClientException
import play.api.Configuration
import com.amazonaws.services.s3.model.{ObjectMetadata, AmazonS3Exception, S3Object}
import java.io.{ByteArrayInputStream, FileInputStream, File, InputStream}


class AmazonSpec extends SpecificationWithJUnit with Specification with Mockito {

	"Amazon configuration" should {
		"configure client only if it's not defined" in {
			val client = mock[Option[S3]]
			val conf = mock[Configuration]

			client.isDefined returns true

			Amazon.client = client
			Amazon.configure (conf)
			
			there was one (client).isDefined
			there was  no (conf).getString (anyString, any)
		}
		"configure client must work correctly" in {
			Amazon.client = None
			val t = Tuple4 ("a1","b2","c3","d4")
			val conf = mock[Configuration]

			conf.getString("amazon.access_key",None) returns Some (t._1)
			conf.getString("amazon.secret_key",None) returns Some (t._2)
			conf.getString("amazon.bucket",None) returns Some (t._3)
			conf.getString("amazon.prefix",None) returns Some(t._4)

			Amazon.configure(conf)
			
			Amazon.client mustNotEqual None
			Amazon.client.isDefined mustEqual true
			Amazon.getAccess mustEqual t._1
			Amazon.getSecret mustEqual t._2
			Amazon.bucket mustEqual t._3
			Amazon.prefix mustEqual t._4
		}
	}

	"Amazon existense check" should {
		"not work if client is not defined" in {
			val client = mock[Option[S3]]
			client.isDefined returns false

			Amazon.has("none") must beEqualTo (false)
			there was no (client).get
		}
		"works correctly with mocking" in {
			val client = mock[Option[S3]]
			val s3 = mock[S3]
			
			s3.getObjectMetadata("bucket","key") returns mock[ObjectMetadata]
			s3.getObjectMetadata("bucket","e") throws new AmazonS3Exception("Not found")
			client.isDefined returns true
			client.get returns s3


			Amazon.bucket = "bucket"
			Amazon.client = client
			Amazon.has("key") must beEqualTo (true)
			Amazon.has("none") must beEqualTo (false)
			Amazon.has("e") must beEqualTo (false)
		}
	}

	"Amazon stream extraction" should {
		"return none when client is not defined" in {
			Amazon.getStream("fake") mustEqual None
		}
		"work correctly with simple mocking" in {
			val client = mock[S3]
			val obj = mock[S3Object]
			val meta = mock[ObjectMetadata]
			val is = mock[InputStream]
			val (key1, bucket1) = Tuple2("key1","bucket1")

			Amazon.bucket = bucket1
			client.getObject(bucket1, key1) returns obj
			obj.getObjectMetadata returns meta
			obj.getObjectContent returns is
			meta.getContentType returns "application/pdf"

			Amazon.client = Some(client)

			Amazon.getStream(key1, None) mustEqual Some(is)
		}
		"process amazon exception correctly" in {
			val client = mock[S3]

			client.getObject(anyString,anyString) throws new AmazonClientException ("fake")

			Amazon.client = Some(client)

			Amazon.getStream("", None) mustEqual None
		}
	}
	
	"Amazon store process" should  {

		"store meta file correctly" in {
			val f1 = Amazon.newByteInputStream
			val f2 = Amazon.newMeta
			val is = mock[ByteArrayInputStream]
			val meta1 = mock[ObjectMetadata]
			val client = mock[S3]
			val key = "my key"
			val text = "Bèjôür"

			Amazon.newMeta = () => meta1
			Amazon.newByteInputStream = (ba:Array[Byte]) => { text.getBytes("utf8") must_== ba; is }
			Amazon.client = Some (client)

			Amazon.storeMeta (key, text)

			there was one (meta1).setContentEncoding("utf-8")
			there was one (meta1).setContentType("application/json")
			there was one (meta1).setContentLength(text.getBytes("utf8").length)
			there was one (client).putObject (Amazon.bucket, key, is, meta1)

			Amazon.newByteInputStream = f1
			Amazon.newMeta = f2
		}

		"store not-existen file" in {
			val hash = "superhash54000.png"
			val f1 = Amazon.tryToHash
			val file = File.createTempFile ("tme", "tmp")
			val client = mock[S3]
			client.getObjectMetadata(Amazon.bucket, hash) returns null

			Amazon.tryToHash = (f:File) => hash

			Amazon.client = Some(client)
			Amazon.store (file, "") must_== "grom-" + hash

			Amazon.tryToHash = f1
			there was one(client).putObject (anyString, anyString, any[InputStream], any[ObjectMetadata])
		}

		"modify an existen file" in {
			val hash = "superhash54000.png"
			val f1 = Amazon.tryToHash
			val f2 = Amazon.newMeta
			val f3 = Amazon.newFileInputStream
			val file = File.createTempFile ("tme", "tmp")
			val is = mock[FileInputStream]
			val client = mock[S3]
			val meta1 = mock[ObjectMetadata]
			val meta2 = mock[ObjectMetadata]

			meta1.getUserMetadata returns Map ("parents"->"1|2|3")
			client.getObjectMetadata(Amazon.bucket, "grom-" + hash) returns meta1
			
			Amazon.tryToHash = (f:File) => hash
			Amazon.newFileInputStream = (f:File) => is
			Amazon.newMeta = () => meta2
			Amazon.client = Some(client)

			Amazon.store  (file, "3") must_== "grom-" + hash

			there was one (client).getObjectMetadata (Amazon.bucket, "grom-" + hash)
			there was one (meta1).getUserMetadata
			there was no (meta2).setUserMetadata (any[java.util.Map[String,String]])
			there was no (client).putObject (anyString, anyString, any[InputStream], any[ObjectMetadata])

			Amazon.store  (file, "4") must_== "grom-" + hash

			there was two (client).getObjectMetadata (Amazon.bucket, "grom-" + hash)
			there was two (meta1).getUserMetadata
			there was one (meta2).setUserMetadata (Map("parents" -> "1|2|3|4"))
			there was one (client).putObject (Amazon.bucket, "grom-" + hash, is, meta2)

			Amazon.newFileInputStream = f3
			Amazon.tryToHash = f1
			Amazon.newMeta = f2
		}

		"dont store an existen file" in {
			val hash = "nohash.png"
			val f1 = Amazon.tryToHash
			val file = new File (File.createTempFile ("tme", "tmp").getCanonicalPath+".png");
			val client = mock[S3]
			client.getObjectMetadata(Amazon.bucket, hash) returns mock[ObjectMetadata]

			Amazon.tryToHash = (f:File) => hash

			Amazon.client = Some(client)
			Amazon.store  (file, "") must_== ""

			Amazon.tryToHash = f1
			there was no(client).putObject (anyString, anyString, any[InputStream], any[ObjectMetadata])
		}


	}

	def mockTwice = Tuple2(mock[Option[S3]],mock[S3])
}
