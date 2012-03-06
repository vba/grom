package tools.storage

import org.specs2.mutable._
import org.mockito.Matchers._
import org.specs2.mock._
import com.amazonaws.services.s3.{AmazonS3Client => S3}

class AmazonSpec extends Specification with Mockito {

//	feature ("Checking stream extraction") {
//		scenario("Simple scenario with mocking") {
//			given ("Mock of amazon client")
//		}
//	}

	"Amazon stream extraction" should {
		"work correctly with simple mocking" in {
			val client = mock[S3]
			Amazon.client = Some(client)
			Amazon.client mustNotEqual None
		}
	}
}
