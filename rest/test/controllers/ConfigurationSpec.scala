package controllers

import org.specs2.mutable._
import org.mockito.Matchers._
import org.specs2.mock._
import tools.Configurable
import play.api.test.{Helpers, FakeRequest}
import play.api.test.Helpers._
import play.api.mvc._
import play.api._
import http.Status

class ConfigurationSpec extends SpecificationWithJUnit
	with Specification
	with Mockito
	with Controller {

	"Config display" should {
		"return forbidden and stop" in {
			val c1 = Configuration.context
			Configuration.context = mock[Configurable]
			Configuration.context.configIsVisible returns false

			val result: SimpleResult[_] = (Configuration.show()(FakeRequest())).asInstanceOf[SimpleResult[_]]

			Configuration.context = c1
			result.header.status must equalTo(403)
		}
	}
}
