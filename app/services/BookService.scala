package services

import daos.BookDAO
import daos.DBTableDefinitions.DBBook
import models.{Book, Folder, User}
import play.api.libs.concurrent.Execution.Implicits._
import scaldi.{Injectable, Injector}

import scala.concurrent.Future


class BookService(implicit inj: Injector) extends Injectable {


  val bookDAO = inject[BookDAO]

  /**
   * Retrieves all user's books
   * @param user current user
   * @return
   */
  def retrieveAll(user: User): Future[List[Book]] = {
    val userId = user.id.getOrElse(throw new ServiceNotAllowedException("User's id is not defined"))

    val dbBooksPromise: Future[List[DBBook]] = bookDAO.findAll(userId)

    dbBooksPromise.map {
      _.map(Book.fromDBBook)
    }
  }

  /**
   * Retrieves book by id (with user checking)
   * @param user current user
   * @param bookId the id of the book to retrieve
   * @return
   */
  def retrieveById(user: User, bookId: Long): Future[Option[Book]] = {
    val userId = user.id.getOrElse(throw new ServiceNotAllowedException("User's id is not defined"))

    val retrievedDBBookPromise: Future[Option[DBBook]] = bookDAO.findById(bookId)

    retrievedDBBookPromise.map {
      _.filter(_.userId == userId).map(Book.fromDBBook)
    }
  }

  /**
   * Saves supplied book for the user
   * Checks if trying to update another user's book (returns Future(None) in this case)
   * @param user current user
   * @param book book to save
   * @return updated book
   */
  def save(user: User, book: Book): Future[Option[Book]] = {
    val userId = user.id.getOrElse(throw new ServiceNotAllowedException("User's id is not defined"))

    // defined id is an indicator that book already exists
    book.id match {
      case Some(_) => saveUpdate(userId, book)
      case None => saveInsert(userId, book)
    }
  }

  private def saveInsert(userId: Long, book: Book): Future[Option[Book]] = {
    val bookToSave = book.toDBBook(userId)

    bookDAO.insert(bookToSave).map(dbBook => Option(Book.fromDBBook(dbBook)))
  }

  private def saveUpdate(userId: Long, book: Book): Future[Option[Book]] = {
    val bookToSave = book.toDBBook(userId)
    val bookId = book.id.get // guaranteed to have id

    bookDAO.findById(bookId).flatMap {
      _.filter(_.userId == userId) match {
        case Some(_) => bookDAO.update(bookToSave).map(dbBook => Option(Book.fromDBBook(dbBook)))
        case None => Future(None) // did not found book with supplied id and that belongs to the current user
      }
    }
  }

  /**
   * Adds book to a supplied folder
   * @param user current user
   * @param book book to add
   * @param folder parent folder
   * @return
   */
  def addToFolder(user: User, book: Book, folder: Folder): Future[Book] = {
    val folderId = folder.id.getOrElse(throw new ServiceNotAllowedException("Parent's folder id is not defined"))
    addToFolder(user, book, folderId)
  }

  /**
   * Adds book to a supplied folder's id
   * @param user current user
   * @param book book to add
   * @param folderId parent folder's id
   * @return
   */
  def addToFolder(user: User, book: Book, folderId: Long): Future[Book] = {
    val userId = user.id.getOrElse(throw new ServiceNotAllowedException("User's id is not defined"))

    val relatedBookPromise: Future[DBBook] = bookDAO.relateBookToFolder(book.toDBBook(userId), folderId)

    relatedBookPromise.map(Book.fromDBBook)
  }

  /**
   * Retrieves all books from a folder
   * @param user current user
   * @param folder parent folder
   * @return a list of Books from a folder
   */
  def retrieveAllFromFolder(user: User, folder: Folder): Future[List[Book]] = {
    val folderId = folder.id.getOrElse(throw new ServiceNotAllowedException("Folder's id is not defined"))
    retrieveAllFromFolder(user, folderId)
  }

  /**
   * Retrieves all books from a folder (by folder's id)
   * @param user current user
   * @param folderId parent folder's id
   * @return a list of Books from a folder (by folder's id)
   */
  def retrieveAllFromFolder(user: User, folderId: Long): Future[List[Book]] = {

    val retrievedDBBooksPromise: Future[List[DBBook]] = bookDAO.findAllInFolder(folderId)

    retrievedDBBooksPromise.map {
      _.map(Book.fromDBBook)
    }

  }

}
