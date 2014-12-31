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


    "return None/Nil if user's id is not supplied" in {
      // Given
      val folderService = new FolderService
      val userWithoutId = UserFixture.testUser.copy(id = None)

      // When
      val retrieveUserFolderTree = await(folderService.retrieveUserFolderTree(userWithoutId))
      val retrieveById = await(folderService.retrieveById(userWithoutId, FolderFixture.rootId))
      val createRootForUser = await(folderService.createRootForUser(userWithoutId))
      val retrieveRoot = await(folderService.retrieveRoot(userWithoutId))
      val appendToRoot = await(folderService.appendToRoot(userWithoutId, "test folder"))
      val appendTo = await(folderService.appendTo(userWithoutId, FolderFixture.rootId, "test folder"))

      // Then
      retrieveUserFolderTree must beEmpty
      retrieveById must beNone
      createRootForUser must beNone
      retrieveRoot must beNone
      appendToRoot must beNone
      appendTo must beNone
    }

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
    }

    "retrieve folder by id" in {
      // Given
      val folderService = new FolderService

      // When
      val folder = await(folderService.retrieveById(UserFixture.testUser, FolderFixture.sub1Id))

      // Then
      folder must beSome
      folder.get.name must beEqualTo(FolderFixture.sub1Name)
    }

    "not retrieve another user's folder" in {
      // Given
      val folderService = new FolderService

      // When
      val folder = await(folderService.retrieveById(UserFixture.testUser, FolderFixture.otherUserRootId))

      // Then
      folder must beNone
    }

  }
}
