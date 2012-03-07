package controllers

import org.specs2.mutable._
import org.mockito.Matchers._
import org.specs2.mock._
import tools.Context
import play.api.Configuration

class ConverterSpec extends Specification with Mockito {

	"Convert method " should {
		"do nothing if storage/stream are not defined" in {
			Context.configure(mock[Configuration])
			Converter.convert("some") must_== None
		}
	}
}
