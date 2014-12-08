package services

import java.util.UUID

import models.BookDAO
import models.DBTableDefinitions.DBBook
import scaldi.{Injectable, Injector}
import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.Future

case class Book(id: Option[Long],
                identifier: UUID,
                name: String,
                format: String,
                totalPages: Int = 0,
                currentPage: Int = 0) {

  def toDBBook(userId: Long): DBBook = {
    DBBook(
      id,
      userId,
      identifier,
      name,
      format,
      totalPages,
      currentPage
    )
  }

}

object Book {
  def fromDBBook(dbBook: DBBook): Book = Book(
    dbBook.id,
    dbBook.uuid,
    dbBook.name,
    dbBook.format,
    dbBook.totalPages,
    dbBook.currentPage
  )
}

class BookService(implicit inj: Injector) extends Injectable {

  val bookDAO = inject[BookDAO]

  /**
   * Retrieves all user's books
   * @param user current user
   * @return
   */
  def retrieveAll(user: User): Future[List[Book]] = {
    val dbBooksPromise = bookDAO.findAll(user)

    dbBooksPromise.map {
      _.map(Book.fromDBBook)
    }
  }

  /**
   * Saves supplied book for the user
   * @param user current user
   * @param book book to save
   * @return
   */
  def save(user: User, book: Book): Future[Book] = {
    val userId = user.id.getOrElse(throw new Exception("User's id is not defined"))
    val bookToSave = book.toDBBook(userId)
    val createdDBBookPromise = bookDAO.insertOrUpdate(bookToSave)

    createdDBBookPromise.map(Book.fromDBBook)
  }

}
