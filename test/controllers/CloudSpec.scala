package controllers

import com.mohiva.play.silhouette.impl.authenticators.SessionAuthenticator
import com.mohiva.play.silhouette.test._
import globals.TestGlobal
import helpers.LivrariumSpecification
import org.specs2.execute.AsResult
import org.specs2.matcher.ThrownMessages
import org.specs2.specification.AroundExample
import play.api.libs.json.{JsNumber, JsString, Json}
import play.api.test._
import services.{FolderContents, FolderService, UserService}

class CloudSpec extends LivrariumSpecification with AroundExample with ThrownMessages{

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


    "return root's content" in {
      // Given
      val folderService = inject[FolderService]
      await(folderService.createRootForUser(TestGlobal.testUser))
      await(folderService.appendToRoot(TestGlobal.testUser, "sub1"))
      await(folderService.appendToRoot(TestGlobal.testUser, "sub2"))


      val request = FakeRequest().withAuthenticator[SessionAuthenticator](TestGlobal.testUser.loginInfo)

      val cloudController = new Cloud

      // When
      val result = cloudController.getRootContent()(request)

      // Then
      status(result) must equalTo(OK)
      contentType(result) must beSome("application/json")
      contentAsJson(result).as[FolderContents].folders must have size 2
    }

    "return some folder's (other than root) content" in {
      // Given
      val folderService = inject[FolderService]
      await(folderService.createRootForUser(TestGlobal.testUser))
      await(folderService.appendToRoot(TestGlobal.testUser, "sub1"))

      val sub2Folder = await(folderService.appendToRoot(TestGlobal.testUser, "sub2"))
      val sub2FolderId = sub2Folder.id.getOrElse(fail("sub-folder's id is not defined"))
      await(folderService.appendTo(TestGlobal.testUser, sub2FolderId, "subSub1"))


      val request = FakeRequest().withAuthenticator[SessionAuthenticator](TestGlobal.testUser.loginInfo)

      val cloudController = new Cloud

      // When
      val result = cloudController.getContent(sub2FolderId)(request)

      // Then
      status(result) must equalTo(OK)
      contentType(result) must beSome("application/json")
      contentAsJson(result).as[FolderContents].folders must have size 1
    }

    "create new folder" in {
      // Given
      val folderService = inject[FolderService]
      await(folderService.createRootForUser(TestGlobal.testUser))

      val requestJson = Json.obj(
        "idParent" -> JsNumber(1),
        "name" -> JsString("testCreateFolder")
      )
      val request = FakeRequest(Helpers.POST, "", FakeHeaders(), requestJson)
        .withAuthenticator[SessionAuthenticator](TestGlobal.testUser.loginInfo)

      val cloudController = new Cloud

      // When
      await(cloudController.createFolder()(request))

      // Then
      val userFolderTree = await(folderService.retrieveUserFolderTree(TestGlobal.testUser))
      userFolderTree must have size 1
    }
  }
}
