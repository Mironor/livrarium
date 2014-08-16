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
  }

  "Folder Model" should {

    "be addable to the database" in new WithFakeApplication {
      Folder.create(Folder(label = "Test Label", children = List()))
      Folder.all().size must equalTo(1)
    }

    "be deletable" in new WithFakeApplication {
      val folderIdOption = Folder.create(Folder(label = "Test Label", children = List()))
      val folderId = folderIdOption.get.toString

      Folder.delete(folderId)

      Folder.all().size must equalTo(0)
    }

    "be able to create and delete complex folder trees" in new WithFakeApplication {
      val folderTree = Folder(label = "Root", children = List(
        Folder(label = "Root 1", children = List(
          Folder(label = "Sub 1 1", children = List(
            Folder(label = "Subsub 1 1 1", children = List()),
            Folder(label = "Subsub 1 1 2", children = List())
          )),
          Folder(label = "Sub 1 2", children = List())
        )),
        Folder(label = "Root 2", children = List(
          Folder(label = "Sut 2", children = List())
        ))
      ))

      val folderTreeIdOption = Folder.create(folderTree)
      val folderTreeId = folderTreeIdOption.get.toString

      Folder.delete(folderTreeId)

      Folder.all().size must equalTo(0)
    }
  }
}

