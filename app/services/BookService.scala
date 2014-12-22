package services

import daos.BookDAO
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
    val userId = user.id.getOrElse(throw new Exception("User's id is not defined"))
    val dbBooksPromise = bookDAO.findAll(userId)

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
    val retrievedDBBookPromise = bookDAO.findById(bookId)

    retrievedDBBookPromise.map {
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

    // Defined id is a good indicator that we should update the model instead of inserting it
    val createdDBBookPromise = bookToSave.id match {
      case Some(_: Long) => bookDAO.update(bookToSave)
      case None => bookDAO.insert(bookToSave)

    }

    createdDBBookPromise.map(Book.fromDBBook)
  }

  /**
   * Adds book to a supplied folder
   * @param user current user
   * @param book book to add
   * @param folder parent folder
   * @return
   */
  def addToFolder(user: User, book: Book, folder: Folder): Future[Book] = {
    val folderId = folder.id.getOrElse(throw new Exception("Parent's folder id is not defined"))
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
    val userId = user.id.getOrElse(throw new Exception("User's id is not defined"))

    val relatedBookPromise = bookDAO.relateBookToFolder(book.toDBBook(userId), folderId)

    relatedBookPromise.map(Book.fromDBBook)
  }

  /**
   * Retrieves all books from a folder
   * @param user current user
   * @param folder parent folder
   * @return a list of Books from a folder
   */
  def retrieveAllFromFolder(user: User, folder: Folder): Future[List[Book]] = {
    val folderId = folder.id.getOrElse(throw new Exception("Folder's id is not defined"))
    retrieveAllFromFolder(user, folderId)
  }

  /**
   * Retrieves all books from a folder (by folder's id)
   * @param user current user
   * @param folderId parent folder's id
   * @return a list of Books from a folder (by folder's id)
   */
  def retrieveAllFromFolder(user: User, folderId: Long): Future[List[Book]] = {

    val retrievedDBBooksPromise = bookDAO.findAllInFolder(folderId)

    retrievedDBBooksPromise.map {
      _.map(Book.fromDBBook)
    }

  }

}
