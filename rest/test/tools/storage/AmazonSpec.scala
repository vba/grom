package tools.storage

import org.specs2.mutable._
import org.mockito.Matchers._
import org.specs2.mock._
import com.amazonaws.services.s3.{AmazonS3Client => S3}
import java.io.InputStream
import com.amazonaws.AmazonClientException
import play.api.Configuration
import com.amazonaws.services.s3.model.{ObjectMetadata, AmazonS3Exception, S3Object}

class AmazonSpec extends Specification with Mockito {


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
			val is = mock[InputStream]
			val (key1, bucket1) = Tuple2("key1","bucket1")

			Amazon.bucket = bucket1
			client.getObject(bucket1, key1) returns obj
			obj.getObjectContent returns is

			Amazon.client = Some(client)

			Amazon.getStream(key1) mustEqual Some(is)
		}
		"process amazon exception correctly" in {
			val client = mock[S3]

			client.getObject(anyString,anyString) throws new AmazonClientException ("fake")
			Amazon.client = Some(client)

			Amazon.getStream("") mustEqual None
		}
	}
}
