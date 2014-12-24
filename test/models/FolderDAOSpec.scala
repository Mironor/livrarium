package models

import daos.FolderDAO
import fixtures.{FolderFixture, UserFixture}
import globals.TestGlobal
import helpers.LivrariumSpecification
import org.specs2.execute.AsResult
import org.specs2.matcher.ThrownMessages
import org.specs2.specification.AroundExample
import play.api.test.FakeApplication

class FolderDAOSpec extends LivrariumSpecification with AroundExample with ThrownMessages {


  /**
   * This automatically handles up and down evolutions at the beginning and at the end of a spec respectively
   */
  def around[T: AsResult](t: => T) = {
    val app = FakeApplication(withGlobal = Some(TestGlobal), additionalConfiguration = inMemoryDatabase())
    running(app) {
      await(UserFixture.initFixture())
      await(FolderFixture.initFixture())

      AsResult(t)
    }
  }

  "Folder DAO" should {
    "find folder's children" in {
      // Given
      val folderDAO = new FolderDAO

      // When
      val rootFolderChildren = await(folderDAO.findChildren(UserFixture.testUser, FolderFixture.root))

      // Then
      rootFolderChildren must have size 2
      rootFolderChildren(0).name must beEqualTo(FolderFixture.sub1Name)
      rootFolderChildren(1).name must beEqualTo(FolderFixture.sub2Name)
    }

    "find folder by id" in {
      // Given
      val folderDAO = new FolderDAO

      // When
      val folder = await(folderDAO.findById(FolderFixture.sub1Id))

      //
      folder must beSome
      folder.get.name must beEqualTo(FolderFixture.sub1Name)
    }

    "affect correct level to appended folder" in {
      // Given
      val folderDAO = new FolderDAO

      // When
      val userFolders = await(folderDAO.findAll(UserFixture.testUser))

      // Then
      userFolders must have size 5

      val userFoldersIds = userFolders.map(_.level)
      userFoldersIds must contain(0, 1, 2, 2, 1).inOrder // This depends on FolderFixture folder tree
    }
  }

}
