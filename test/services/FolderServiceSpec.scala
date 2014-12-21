package services

import fixtures.{UserFixture, FolderFixture}
import globals.TestGlobal
import helpers.LivrariumSpecification
import org.specs2.execute.AsResult
import org.specs2.matcher.ThrownMessages
import org.specs2.specification.AroundExample
import play.api.test.FakeApplication


class FolderServiceSpec extends LivrariumSpecification with AroundExample with ThrownMessages {

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

  "Folder service" should {

    "return user's folder tree" in {
      // Given
      val folderService = new FolderService

      // When
      val rootFolderChildren = await(folderService.retrieveUserFolderTree(UserFixture.testUser))

      // Then
      rootFolderChildren must have size 2

      val sub1 = rootFolderChildren(0)
      sub1.name must beEqualTo(FolderFixture.sub1Name)
      sub1.children must have size 2

      val subSub1 = sub1.children(0)
      subSub1.name must beEqualTo(FolderFixture.sub1sub1Name)

      val subSub2 = sub1.children(1)
      subSub2.name must beEqualTo(FolderFixture.sub1sub2Name)

      val sub2 = rootFolderChildren(1)
      sub2.name must beEqualTo(FolderFixture.sub2Name)
      sub2.children must have size 0
    }

    "append a folder to root" in {
      // Given
      val folderService = new FolderService
      val testFolderName = "testFolder"

      // When
      await(folderService.appendToRoot(UserFixture.testUser, testFolderName))
      val rootFolderChildren = await(folderService.retrieveChildren(UserFixture.testUser, FolderFixture.rootId))

      // Then
      rootFolderChildren must have size 3

      val testFolder = rootFolderChildren(2)
      testFolder.name must beEqualTo(testFolderName)
      testFolder.children must have size 0
    }

    "retrieve folder's children" in {
      // Given
      val folderService = new FolderService

      // When
      val sub1FolderChildren = await(folderService.retrieveChildren(UserFixture.testUser, FolderFixture.sub1Id))

      // Then
      sub1FolderChildren must have size 2

      val subSub1 = sub1FolderChildren(0)
      subSub1.name must beEqualTo(FolderFixture.sub1sub1Name)

      val subSub2 = sub1FolderChildren(1)
      subSub2.name must beEqualTo(FolderFixture.sub1sub2Name)
    }

    "append a folder to another folder (not root)" in {
      // Given
      val folderService = new FolderService
      val testFolderName = "testFolder"

      // When
      await(folderService.appendTo(UserFixture.testUser, FolderFixture.sub1Id, testFolderName))
      val sub1Children = await(folderService.retrieveChildren(UserFixture.testUser, FolderFixture.sub1Id))

      // Then
      sub1Children must have size 3

      val testFolder = sub1Children(2)
      testFolder.name must beEqualTo(testFolderName)
      testFolder.children must have size 0
    }

  }
}
