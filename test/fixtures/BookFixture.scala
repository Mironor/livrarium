package fixtures

import java.util.UUID

import daos.DBTableDefinitions
import helpers.BookFormatHelper
import DBTableDefinitions._
import play.api.Play.current
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick._

import scala.concurrent.Future
import scala.slick.lifted.TableQuery

object BookFixture {

  val slickBooks = TableQuery[Books]
  val slickBookToFolder = TableQuery[BooksToFolders]

  val rootBookName = "root book"
  val rootBookId = 1
  val rootBookUUID = UUID.fromString("00000000-0000-4000-A000-100000000000")
  val rootBook = DBBook(
    Option(rootBookId),
    UserFixture.testUserId,
    rootBookUUID,
    rootBookName,
    BookFormatHelper.PDF
  )

  val rootBookToFolderLink = BookToFolder(rootBookId, FolderFixture.rootId)


  val sub1BookName = "sub 1 book"
  val sub1BookId = 2
  val sub1BookUUID = UUID.fromString("00000000-0000-4000-A000-200000000000")
  val sub1Book = DBBook(
    Option(sub1BookId),
    UserFixture.testUserId,
    sub1BookUUID,
    sub1BookName,
    BookFormatHelper.PDF
  )

  val sub1BookToFolderLink = BookToFolder(sub1BookId, FolderFixture.sub1Id)

  val otherUserBookName = "sub 1 book"
  val otherUserBookId = 3
  val otherUserBookUUID = UUID.fromString("00000000-0000-4000-A000-300000000000")
  val otherUserBook = DBBook(
    Option(otherUserBookId),
    UserFixture.otherUserId,
    otherUserBookUUID,
    otherUserBookName,
    BookFormatHelper.PDF
  )

  val otherUserBookToFolderLink = BookToFolder(otherUserBookId, FolderFixture.otherUserRootId)


  def initFixture(): Future[_] = {
    Future.successful {
      DB withSession { implicit session =>
        slickBooks ++= Seq(rootBook, sub1Book, otherUserBook)
        slickBookToFolder ++= Seq(rootBookToFolderLink, sub1BookToFolderLink, otherUserBookToFolderLink)
      }
    }
  }

}
