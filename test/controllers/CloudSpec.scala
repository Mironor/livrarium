package controllers

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import com.mohiva.play.silhouette.test._
import play.api.test.{FakeRequest, PlaySpecification}
import scaldi.Injectable
import services.User

class CloudSpec extends PlaySpecification with Injectable{

  "Cloud controller" should {
    "create new folder" in {
      val user = User(Option(1), LoginInfo("key", "value"), None, None)
      implicit val env = FakeEnvironment[User, CookieAuthenticator](user)
      val request = FakeRequest().withAuthenticator(user.loginInfo)

      val controller = new UserController(env)
      val result = controller.isAuthenticated(request)

      status(result) must equalTo(OK)
    }
  }
}
