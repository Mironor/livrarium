package models

import daos.DBTableDefinitions.DBFolder
import fixtures.UserFixture
import play.api.test.PlaySpecification

class FolderSpec extends PlaySpecification{

  "Folder model" should {
    "be creatable from dbFolder" in {
      // Given
      val folderName = "root"
      val dbFolder = DBFolder(Option(1), UserFixture.testUserId, folderName, 0, 0, 1)

      // When
      val folder = Folder.fromDBFolder(dbFolder)

      // Then
      folder.id must beEqualTo(1)
      folder.name must beEqualTo(folderName)
      folder.children must beEmpty
    }

    "throw an exception if created from dbFolder that has no id" in {
      // Given
      val folderName = "root"
      val dbFolder = DBFolder(None, UserFixture.testUserId, folderName, 0, 0, 1)

      // When // Then
      Folder.fromDBFolder(dbFolder) must throwA[Exception]
    }
  }
}
