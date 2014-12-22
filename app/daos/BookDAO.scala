package daos

import daos.DBTableDefinitions.{BookToFolder, Books, BooksToFolders, DBBook}
import play.api.Play.current
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick._

import scala.concurrent.Future

class BookDAO {
  val slickBooks = TableQuery[Books]
  val slickBooksToFolders = TableQuery[BooksToFolders]

  /**
   * Finds all books for a specified userId
   * @param userId user's id
   * @return
   */
  def findAll(userId: Long): Future[List[DBBook]] = {
    Future.successful {
      DB withSession { implicit session =>
        slickBooks.filter(_.idUser === userId).list
      }
    }
  }

  /**
   * Finds book by its id
   * @param bookId book's id
   * @return
   */
  def findById(bookId: Long): Future[Option[DBBook]] = {
    Future.successful {
      DB withSession { implicit session =>
        slickBooks.filter(_.id === bookId).firstOption
      }
    }
  }

  /**
   * Finds all books for a specified folderId
   * No checking is done that the folder is associated with current user, use Service instead
   * @param folderId parent folder's id
   * @return
   */
  def findAllInFolder(folderId: Long): Future[List[DBBook]] = {
    Future.successful {
      DB withSession { implicit session =>
        (for {
          bookToFolder <- slickBooksToFolders if bookToFolder.idFolder === folderId
          book <- slickBooks if book.id === bookToFolder.idBook
        } yield book).list
      }
    }
  }

  /**
   * Inserts new book 
   * No checking is done that the book is associated with current user, use Service instead
   * @param book book to update/insert
   * @return
   */
  def insert(book: DBBook): Future[DBBook] = {
    Future.successful {
      DB withSession { implicit session =>
        val insertedBookId = (slickBooks returning slickBooks.map(_.id)) += book
        book.copy(id = Option(insertedBookId))
      }
    }
  }

  /**
   * Updates book
   * No checking is done that the book is associated with current user, use Service instead
   * @param book book to update/insert
   * @return
   */
  def update(book: DBBook): Future[DBBook] = {
    Future.successful {
      DB withSession { implicit session =>
        slickBooks.filter(_.id === book.id).update(book)
        book
      }
    }
  }

  /**
   * Creates a relation book <=> folder
   * No checking is done that the book and/or folder are associated with current user, use Service instead
   * @param book book's id
   * @param folderId folder's id
   * @return
   */
  def relateBookToFolder(book: DBBook, folderId: Long): Future[DBBook] = {
    Future.successful {
      DB withSession { implicit session =>
        val bookId = book.id.getOrElse(throw new DAOException("Book's id is not defined"))
        slickBooksToFolders += BookToFolder(bookId, folderId)
        book
      }
    }
  }
}
