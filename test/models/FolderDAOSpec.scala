package models

import globals.TestGlobal
import helpers.LivrariumSpecification
import org.specs2.execute.AsResult
import org.specs2.matcher.ThrownMessages
import org.specs2.specification.AroundExample
import play.api.test.FakeApplication
import services.UserService

class FolderDAOSpec extends LivrariumSpecification with AroundExample with ThrownMessages {


  /**
   * This automatically handles up and down evolutions at the beginning and at the end of a spec respectively
   */
  def around[T: AsResult](t: => T) = {
    val app = FakeApplication(withGlobal = Some(TestGlobal), additionalConfiguration = inMemoryDatabase())
    running(app) {
      val userService = inject[UserService]
      await(userService.saveWithLoginInfo(TestGlobal.testUser))
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

    val folderDAO = new FolderDAO

    val rootFolder = await(folderDAO.insertRoot(TestGlobal.testUser))
    val sub1Folder = await(folderDAO.appendToFolder(TestGlobal.testUser, rootFolder, "Sub1"))
    await(folderDAO.appendToFolder(TestGlobal.testUser, rootFolder, "Sub2"))
    await(folderDAO.appendToFolder(TestGlobal.testUser, sub1Folder, "SubSub1"))
    await(folderDAO.appendToFolder(TestGlobal.testUser, sub1Folder, "SubSub2"))
  }

  "Folder DAO" should {
    "find folder's children" in {
      // Given
      val folderDAO = new FolderDAO

      // When
      val rootFolderOption = await(folderDAO.findRoot(TestGlobal.testUser))
      val rootFolder = rootFolderOption.getOrElse(fail("root folder cannot be found"))
      val rootFolderChildren = await(folderDAO.findChildren(TestGlobal.testUser, rootFolder))

      // Then
      rootFolderChildren must have size 2
      rootFolderChildren(0).name must beEqualTo("Sub1")
      rootFolderChildren(1).name must beEqualTo("Sub2")
    }

    "affect correct level to appended folder" in {
      // Given
      val folderDAO = new FolderDAO

      // When
      val userFolders = await(folderDAO.findAll(TestGlobal.testUser))

      // Then
      userFolders must have size 5

      val userFoldersIds = userFolders.map(_.level)
      userFoldersIds must contain(0, 1, 2, 2, 1).inOrder // This depends on constructUserTree function
    }
  }

}
