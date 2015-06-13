package daos

import daos.DBTableDefinitions.{BookToFolder, Books, BooksToFolders, DBBook}
import play.api.Play
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfig}
import slick.driver.JdbcProfile
import slick.lifted.TableQuery
import slick.driver.PostgresDriver.api._

import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future

class BookDAO extends HasDatabaseConfig[JdbcProfile] {
  val dbConfig = DatabaseConfigProvider.get[JdbcProfile](Play.current)

  val slickBooks = TableQuery[Books]
  val slickBooksToFolders = TableQuery[BooksToFolders]

  /**
   * Finds all books for a specified userId
   * @param userId user's id
   * @return a promise with a list of user's folders
   */
  def findAll(userId: Long): Future[Seq[DBBook]] = db.run {
    slickBooks.result
  }

  /**
   * Finds book by its id
   * @param bookId book's id
   * @return a promise with a found book (None if no book was found)
   */
  def findById(bookId: Long): Future[Option[DBBook]] = db.run {
    slickBooks.filter(_.id === bookId).result.headOption
  }

  /**
   * Finds all books for a specified folderId
   * @param folderId parent folder's id
   * @return a promise of a list of books contained in the folder
   */
  def findAllInFolder(folderId: Long): Future[Seq[DBBook]] = db.run {
    (for {
      bookToFolder <- slickBooksToFolders if bookToFolder.idFolder === folderId
      book <- slickBooks if book.id === bookToFolder.idBook
    } yield book).result
  }

  /**
   * Inserts the supplied book
   * @param book book to update/insert
   * @return a promise of the inserted book
   */
  def insert(book: DBBook): Future[DBBook] = db.run {
    (slickBooks returning slickBooks.map(_.id)) += book
  }.map(insertedBookId => book.copy(id = Option(insertedBookId)))

  /**
   * Updates book
   * No checking is done that the book is associated with current user, use Service instead
   * @param book book to update/insert
   * @return a promise of the updated book
   */
  def update(book: DBBook): Future[DBBook] = db.run {
    slickBooks.filter(_.id === book.id).update(book)
  }.map(_ => book)

  /**
   * Creates a relation book <=> folder
   * No checking is done that the book and/or folder are associated with current user, use Service instead
   * @param bookId book's id
   * @param folderId folder's id
   * @return a promise of the link between the book and the folder
   */
  def relateBookToFolder(bookId: Long, folderId: Long): Future[BookToFolder] = db.run {
    slickBooksToFolders += BookToFolder(bookId, folderId)
  }.map(_ => BookToFolder(bookId, folderId))
}
