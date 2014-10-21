package models

import com.mohiva.play.silhouette.core.LoginInfo
import helpers.WithFakeApplication
import org.specs2.mutable._
import scaldi.{Injectable, Injector}

class FolderSpec(implicit inj: Injector) extends Specification with Injectable{

  val folderDAO = inject [FolderDAO]

  val user = User(Option(1), LoginInfo("key", "value"), None, None)

  "Folder model" should {

    "be addable to the database" in new WithFakeApplication {
      "Hello world" must have size 11
    }

  }
}
