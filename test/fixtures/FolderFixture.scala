package fixtures

import daos.DBTableDefinitions.{DBFolder, Folders}
import slick.driver.PostgresDriver.api._
import slick.lifted.TableQuery

import scala.concurrent.Future

/**
 * Initialise basic folder hierarchy with each folder's id available
 *
 * PLEASE KEEP UP TO DATE
 * Mocked tree:
 * root
 * **Sub1
 * ****SubSub1
 * ****SubSub2
 * **Sub2
 *
 */
object FolderFixture {
  lazy val database = Database.forConfig("slick.dbs.default.db")

  val slickFolders = TableQuery[Folders]

  val rootId = 1
  val rootName = "__root__"
  val root = DBFolder(Option(rootId), UserFixture.testUserId, rootName, 0, 0, 9)

  val sub1Id = 2
  val sub1Name = "Sub1"
  val sub1 = DBFolder(Option(sub1Id), UserFixture.testUserId, sub1Name, 1, 1, 6)

  val sub1sub1Id = 3
  val sub1sub1Name = "Sub1Sub1"
  val sub1sub1 = DBFolder(Option(sub1sub1Id), UserFixture.testUserId, sub1sub1Name, 2, 2, 3)

  val sub1sub2Id = 4
  val sub1sub2Name = "Sub1Sub2"
  val sub1sub2 = DBFolder(Option(sub1sub2Id), UserFixture.testUserId, sub1sub2Name, 2, 4, 5)

  val sub2Id = 5
  val sub2Name = "Sub2"
  val sub2 = DBFolder(Option(sub2Id), UserFixture.testUserId, sub2Name, 1, 7, 8)

  val otherUserRootId = 6
  val otherUserRootName = "__root__"
  val otherUserRoot = DBFolder(Option(otherUserRootId), UserFixture.otherUserId, otherUserRootName, 0, 0, 1)

  def initFixture(): Future[_] = database.run {
    slickFolders ++= Seq(root, sub1, sub1sub1, sub1sub2, sub2, otherUserRoot)
  }
}
