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
import libs.concurrent.Promise
import libs.iteratee.{Iteratee, Enumerator}

class ConfigurationSpec extends SpecificationWithJUnit
	with Specification
	with Mockito
	with Controller {

	"Config display" should {
		"respond correctly to his route and return forbidden if display is not allowed" in {
			Configuration.context = mock[Configurable]
			Configuration.context.configIsVisible returns false

			val Some(result) = routeAndCall(FakeRequest(GET, "/config/show"))
			val sr = result.asInstanceOf[SimpleResult[_]]
			sr.header.status must equalTo(403)
		}
		"display configuration if it's allowed" in {
			Configuration.context = mock[Configurable]
			Configuration.context.configIsVisible returns true

			val c1 = Configuration.conf
			Configuration.conf = (s:String) => s + "\t[NA]\n"

			val Some(result) = routeAndCall(FakeRequest(GET, "/config/show"))
			val sr = result.asInstanceOf[SimpleResult[String]]

			Configuration.conf = c1
			val bp:Promise[String] = sr.body(Iteratee.consume[String]()).flatMap(i=> i.run)

			bp.onRedeem (s=> {
				val factor = s.contains("storage.type\t[NA]") &&
					s.contains("amazon.access_key\t[NA]") &&
					s.contains("amazon.secret_key\t[NA]") &&
					s.contains("amazon.bucket\t[NA]") &&
					s.contains("amazon.prefix\t[NA]") &&
					s.contains("fs.inbox\t[NA]") &&
					s.contains("fs.outbox\t[NA]") &&
					s.contains("conversion.scale\t[NA]") &&
					s.contains("conversion.preview_scale\t[NA]") &&
					s.contains("conversion.libre_office\t[NA]") &&
					s.contains("conversion.allow_config_display\t[NA]")
				factor must equalTo(true)
			})
			sr.header.status must equalTo(200)
		}
	}
}
