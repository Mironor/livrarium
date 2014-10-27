package models

import com.mohiva.play.silhouette.core.LoginInfo
import modules.{SilhouetteModule, WebModule}
import org.specs2.execute.AsResult
import org.specs2.specification.AroundExample
import play.api.GlobalSettings
import play.api.test.{FakeApplication, PlaySpecification}
import scaldi.Injectable
import scaldi.play.ScaldiSupport
import services.{Folder, UserService, FolderService, User}


class FolderServiceSpec extends PlaySpecification with AroundExample with Injectable {


  val user = User(Option(1), LoginInfo("key", "value"), None, None)

  object TestGlobal extends GlobalSettings with ScaldiSupport {
    def applicationModule = new WebModule :: new SilhouetteModule
  }

  implicit val injector = TestGlobal.applicationModule
  val userService = inject[UserService]
  val folderService = inject[FolderService]

  def around[T: AsResult](t: => T) = {
    val app = FakeApplication(additionalConfiguration = inMemoryDatabase())
    running(app) {
      await(userService.saveWithLoginInfo(user))
      await(folderService.createRootForUser(user))
      val sub1Folder = await(folderService.appendToRoot(user, "Sub1"))
      await(folderService.appendToRoot(user, "Sub2"))
      await(folderService.appendTo(user, sub1Folder, "SubSub1"))
      await(folderService.appendTo(user, sub1Folder, "SubSub2"))

      AsResult(t)
    }
  }

  "Folder service" should {

    "return user's folder tree" in {
      val rootFolderChildren = await(folderService.retrieveUserFolderTree(user))
      rootFolderChildren must have size 2
      rootFolderChildren(0).children must have size 2
      rootFolderChildren(1).children must have size 0
    }



  }
}
