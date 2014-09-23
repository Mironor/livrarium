package services

import models.{BookDAO, Book}
import scaldi.{Injectable, Injector}

import scala.concurrent.Future

/**
 * Handles actions with books
 *
 */
class BookService(implicit inj: Injector)  extends Injectable {
  val bookDAO  = inject[BookDAO]

  def retrieve(id: String): Future[Option[Book]] = bookDAO.find(id)

  def save(book: Book) = {
    bookDAO.save(book)
  }
}
