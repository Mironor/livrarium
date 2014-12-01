package controllers

import com.mohiva.play.silhouette.api.{Authenticator, Environment}
import com.mohiva.play.silhouette.impl.authenticators.SessionAuthenticator
import com.mohiva.play.silhouette.test._
import globals.TestGlobal
import org.specs2.execute.AsResult
import org.specs2.specification.AroundExample
import play.api.libs.json.{JsNumber, JsString, Json}
import play.api.test._
import scaldi.{Injectable, Injector}
import services.{FolderService, User, UserService}

class CloudSpec extends PlaySpecification with AroundExample with Injectable {

  implicit def injector: Injector = TestGlobal.injector

  /**
   * This automatically handles up and down evolutions at the beginning and at the end of a spec respectively
   */
  def around[T: AsResult](t: => T) = {
    val app = FakeApplication(withGlobal = Some(TestGlobal), additionalConfiguration = inMemoryDatabase())
    running(app) {
      val userService = inject[UserService]
      await(userService.saveWithLoginInfo(TestGlobal.testUser))
      AsResult(t)
    }
  }

  "Cloud controller" should {
    "create new folder" in {
      // Given
      implicit val env: Environment[User, SessionAuthenticator] = inject[Environment[User, SessionAuthenticator]]
      val folderService = inject[FolderService]
      await(folderService.createRootForUser(TestGlobal.testUser))

      val requestJson = Json.obj(
        "idParent" -> JsNumber(1),
        "name" -> JsString("testCreateFolder")
      )
      val request = FakeRequest(Helpers.POST, routes.Cloud.createFolder().url, FakeHeaders(),
        requestJson).withAuthenticator[SessionAuthenticator](TestGlobal.testUser.loginInfo)

      val cloudController = new Cloud

      // When
      await(cloudController.createFolder()(request))

      // Then
      val userFolderTree = await(folderService.retrieveUserFolderTree(TestGlobal.testUser))
      userFolderTree must have size 1
    }
  }
}
