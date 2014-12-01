package services

import com.mohiva.play.silhouette.api.LoginInfo
import globals.TestGlobal
import modules.{SilhouetteModule, WebModule}
import org.specs2.execute.AsResult
import org.specs2.specification.AroundExample
import play.api.GlobalSettings
import play.api.test.{FakeApplication, PlaySpecification}
import scaldi.{Injector, Injectable}
import scaldi.play.ScaldiSupport


class FolderServiceSpec extends PlaySpecification with AroundExample with Injectable {

  val user = User(Option(1), LoginInfo("key", "value"), None, None)


  implicit def injector: Injector = TestGlobal.injector

  /**
   * This automatically handles up and down evolutions at the beginning and at the end of a spec respectively
   */
  def around[T: AsResult](t: => T) = {
    val app = FakeApplication(withGlobal = Some(TestGlobal), additionalConfiguration = inMemoryDatabase())
    running(app) {
      val userService = inject[UserService]
      await(userService.saveWithLoginInfo(user))
      constructUserTree()

      AsResult(t)
    }
  }

  private def constructUserTree() = {

    /**
     * PLEASE KEEP UP TO DATE
     * Mocked tree:
     * root
     *   Sub1
     *     SubSub1
     *     SubSub2
     *   Sub2
     */
    val folderService = new FolderService

    await(folderService.createRootForUser(user))
    val sub1Folder = await(folderService.appendToRoot(user, "Sub1"))
    await(folderService.appendToRoot(user, "Sub2"))
    await(folderService.appendTo(user, sub1Folder, "SubSub1"))
    await(folderService.appendTo(user, sub1Folder, "SubSub2"))
  }

  "Folder service" should {

    "return user's folder tree" in {
      // Given
      val folderService = new FolderService

      // When
      val rootFolderChildren = await(folderService.retrieveUserFolderTree(user))

      // Then
      rootFolderChildren must have size 2

      val sub1 = rootFolderChildren(0)
      sub1.name must beEqualTo("Sub1")
      sub1.children must have size 2

      val subSub1 = sub1.children(0)
      subSub1.name must beEqualTo("SubSub1")

      val subSub2 = sub1.children(1)
      subSub2.name must beEqualTo("SubSub2")

      val sub2 = rootFolderChildren(1)
      sub2.name must beEqualTo("Sub2")
      sub2.children must have size 0
    }

    "append a folder to root" in {
      // Given
      val folderService = new FolderService
      val testFolderName = "testFolder"

      // When
      await(folderService.appendToRoot(user, testFolderName))
      val rootFolderChildren = await(folderService.retrieveUserFolderTree(user))

      // Then
      rootFolderChildren must have size 3

      val testFolder = rootFolderChildren(2)
      testFolder.name must beEqualTo(testFolderName)
      testFolder.children must have size 0
    }

    "append a folder to another folder (not root)" in {
      // Given
      val folderService = new FolderService
      val testFolderName = "testFolder"

      val rootFolderChildrenBeforeAppend = await(folderService.retrieveUserFolderTree(user))
      val sub1BeforeAppend = rootFolderChildrenBeforeAppend(0)

      // When
      await(folderService.appendTo(user, sub1BeforeAppend, testFolderName))
      val rootFolderChildren = await(folderService.retrieveUserFolderTree(user))

      // Then
      val sub1 = rootFolderChildren(0)
      sub1.children must have size 3

      val testFolder = sub1.children(2)
      testFolder.name must beEqualTo(testFolderName)
      testFolder.children must have size 0
    }

  }
}
