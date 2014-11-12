package controllers

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.authenticators.{SessionAuthenticator, CookieAuthenticator}
import com.mohiva.play.silhouette.test._
import modules.{SilhouetteModule, WebModule}
import org.specs2.execute.AsResult
import org.specs2.specification.AroundExample
import play.api.GlobalSettings
import play.api.test.{FakeApplication, FakeRequest, PlaySpecification}
import scaldi.Injectable
import scaldi.play.{ControllerInjector, ScaldiSupport}
import services.User

class CloudSpec extends PlaySpecification with AroundExample with Injectable {

  object TestGlobal extends GlobalSettings with ScaldiSupport {
    def applicationModule = new WebModule :: new SilhouetteModule
  }

  implicit lazy val injector = TestGlobal.injector


  /**
   * This automatically handles up and down evolutions at the beginning and at the end of a spec respectively
   */
  def around[T: AsResult](t: => T) = {
    val app = FakeApplication(withGlobal = Some(TestGlobal), additionalConfiguration = inMemoryDatabase() ++ Map(
      "silhouette.authenticator.sessionKey" -> "livrarium-auth-test",
      "silhouette.authenticator.encryptAuthenticator" -> true,
      "silhouette.authenticator.useFingerprinting" -> true,
      "silhouette.authenticator.authenticatorIdleTimeout" -> 1800,
      "silhouette.authenticator.authenticatorExpiry" -> 43200
    ))

    running(app) {


      AsResult(t)
    }
  }

  "Cloud controller" should {
    "work" in {

      val user = User(Option(1), LoginInfo("key", "value"), None, None)
      implicit val env = FakeEnvironment[User, SessionAuthenticator](user)
      val request = FakeRequest().withAuthenticator(user.loginInfo)


      val controller = new Application
      val result = controller.index(request)

      status(result) must equalTo(OK)
    }
  }
}
