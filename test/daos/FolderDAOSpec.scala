package daos

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
      val rootFolderChildren = await(folderDAO.findChildrenById(FolderFixture.rootId))

      // Then
      rootFolderChildren must have size 2
      rootFolderChildren(0).name must beEqualTo(FolderFixture.sub1Name)
      rootFolderChildren(1).name must beEqualTo(FolderFixture.sub2Name)
    }

    "find folder by id" in {
      // Given
      val folderDAO = new FolderDAO

      // When
      val folder = await(folderDAO.findById(FolderFixture.sub1Id)).getOrElse(fail("Folder was not found"))

      // Then
      folder.name must beEqualTo(FolderFixture.sub1Name)
    }

    "affect correct level to appended folder" in {
      // Given
      val folderDAO = new FolderDAO

      // When
      val root = await(folderDAO.findById(FolderFixture.rootId)).getOrElse(fail("Folder was not found"))
      val sub1 = await(folderDAO.findById(FolderFixture.sub1Id)).getOrElse(fail("Folder was not found"))
      val sub1sub1 = await(folderDAO.findById(FolderFixture.sub1sub1Id)).getOrElse(fail("Folder was not found"))
      val sub1sub2 = await(folderDAO.findById(FolderFixture.sub1sub2Id)).getOrElse(fail("Folder was not found"))
      val sub2 = await(folderDAO.findById(FolderFixture.sub2Id)).getOrElse(fail("Folder was not found"))

      // Then
      root.level must beEqualTo(0)
      sub1.level must beEqualTo(1)
      sub1sub1.level must beEqualTo(2)
      sub1sub2.level must beEqualTo(2)
      sub2.level must beEqualTo(1)
    }
  }

}
