package tools

import dto.Key
import extractors.Extractable
import org.specs2.mutable._
import org.mockito.Matchers._
import org.specs2.mock._
import play.api.Logger

class ContextSpec extends SpecificationWithJUnit with Specification with Mockito {


	"Process keys" should {

		"work as expected with one simple request" in {

			val key1 = mock[Key]
			val extract1 = mock[Extractable]

			key1.id returns "1"
			key1.extractor returns Some (extract1)

			Context.keysToProcess add key1
			Context.processKeys()

			there was atLeastOne (key1).id
			there was one (extract1).extract ("1")

			Context.keysInProcess.size must_== 0
			Context.keysToProcess.size must_== 0
		}

		"work as expected with a lot of duplicated requests" in {

			val key2 = mock[Key]
			val extract2 = mock[Extractable]

			key2.id returns "2"
			key2.extractor throws new IllegalStateException("Oups")
//			key2.extractor returns Some (extract2)

			Context.keysInProcess add key2
			Context.processKeys()
			Context.keysToProcess.size must_== 0

			there was no (key2).id

			Context.keysToProcess add key2
			Context.processKeys()

			there was no (key2).id
			there was no (extract2).extract (anyString)

			Context.keysToProcess.empty
			Context.keysInProcess.empty

			Context.keysToProcess add key2
			
			try {Context.processKeys(); failure("Cannot be here")}
			catch { case _ => }

			Context.keysToProcess contains key2
			Context.keysInProcess contains key2
		}
	}
}
