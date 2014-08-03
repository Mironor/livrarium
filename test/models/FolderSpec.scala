package models


import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._

import com.mongodb.casbah.commons.MongoDBObject
import play.api.PlayException
import org.specs2.specification.BeforeExample

import helpers._


@RunWith(classOf[JUnitRunner])
class FolderSpec extends Specification with BeforeExample {

  def before = new WithFakeApplication {
    FolderDAO.remove(MongoDBObject.empty)
    println("before")
    println(Folder.all().size)
  }

  "Folder Model" should {

    "be addable to the database" in new WithFakeApplication {
      println("addable")
      print(Folder.all().size)
      Folder.create("Test Label", 0, 1)
      Folder.all().size must equalTo(1)
    }

    "be deletable" in new WithFakeApplication {
      println("deletable")
      print(Folder.all().size)
      val folderIdOption = Folder.create("Test Label", 0, 1)
      val folderId = folderIdOption.getOrElse(throw new PlayException(
        "Database error",
        "Could not get the ID of the created task, check your database configuration"
      )).toString

      Folder.delete(folderId)

      Folder.all().size must equalTo(0)
    }

    "be able to give ancestors of a specific folder" in new WithFakeApplication {
      println("children")
      print(Folder.all().size)
      Folder.create("Root 1", 0, 9)
      val parentId = Folder.create("Sub 1 1", 1, 6)
      Folder.create("Subsub 1 1 1", 2, 3)
      Folder.create("Subsub 1 1 2", 4, 5)
      Folder.create("Sub 1 2", 7, 8)

      Folder.create("Root 2", 10, 13)
      Folder.create("Sut 2", 11, 12)

      val parent = Folder.find(parentId.get.toString).getOrElse{
        throw new Exception("No folder found")
      }

      val children = Folder.getChildrenOf(parent)

      children.size must equalTo(2)
      children.map(_.name) must contain("Subsub 1 1 1", "Subsub 1 1 2")
    }
  }
}

