package services

import daos.BookDAO
import models.{Book, Folder, User}
import play.api.libs.concurrent.Execution.Implicits._
import scaldi.{Injectable, Injector}

import scala.concurrent.Future


class BookService(implicit inj: Injector) extends Injectable {


  val bookDAO = inject[BookDAO]

  val folderService = inject[FolderService]

  /**
   * Retrieves all user's books
   * @param user current user
   * @return
   */
  def retrieveAll(user: User): Future[List[Book]] = {
    user.id match {
      case Some(userId) => bookDAO.findAll(userId).map {
        _.map(Book.fromDBBook)
      }

      case None => Future(Nil)
    }
  }

  /**
   * Retrieves book by id (with user checking)
   * @param user current user
   * @param bookId the id of the book to retrieve
   * @return
   */
  def retrieveById(user: User, bookId: Long): Future[Option[Book]] = {
    user.id match {
      case Some(userId) => bookDAO.findById(bookId).map {
        _.filter(_.userId == userId).map(Book.fromDBBook)
      }
      case None => Future(None)
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
    user.id match {
      case Some(userId) =>
        // defined id is an indicator that book already exists
        book.id match {
          case Some(bookId) => saveUpdate(userId, book, bookId)
          case None => saveInsert(userId, book)
        }

      case None => Future(None)
    }
  }

  private def saveUpdate(userId: Long, book: Book, bookId: Long): Future[Option[Book]] = {
    val bookToSave = book.toDBBook(userId)

    bookDAO.findById(bookId).flatMap {
      _.filter(_.userId == userId) match {
        case Some(_) => bookDAO.update(bookToSave).map(dbBook => Option(Book.fromDBBook(dbBook)))
        case None => Future(None) // The book with supplied id belongs to another user
      }
    }
  }

  private def saveInsert(userId: Long, book: Book): Future[Option[Book]] = {
    val bookToSave = book.toDBBook(userId)

    bookDAO.insert(bookToSave).map(dbBook => Option(Book.fromDBBook(dbBook)))
  }

  /**
   * Adds book to a supplied folder
   * @param user current user
   * @param book book to add
   * @param folder parent folder
   * @return added book
   */
  def addToFolder(user: User, book: Book, folder: Folder): Future[Option[Book]] = {
    folder.id match {
      case Some(folderId) => addToFolder(user, book, folderId)
      case None => Future(None)
    }
  }

  /**
   * Adds book to a supplied folder's id
   * @param user current user
   * @param book book to add
   * @param folderId parent folder's id
   * @return added book
   */
  def addToFolder(user: User, book: Book, folderId: Long): Future[Option[Book]] = {
    book.id match {
      case Some(bookId) => addToFolder(user, bookId, folderId)
      case None => Future(None)
    }
  }

  /**
   * Adds book (by id) to the folder (also by id)
   * @param user current user
   * @param bookId id of the book that is to add to the folder
   * @param folderId  parent folder's id
   * @return
   */
  def addToFolder(user: User, bookId: Long, folderId: Long): Future[Option[Book]] = {
    user.id match {
      case Some(userId) =>
        val book = retrieveById(user, bookId)
        val folder = folderService.retrieveById(user, folderId)

        book zip folder flatMap {
          case (Some(retrievedBook), Some(_)) =>
            bookDAO.relateBookToFolder(retrievedBook.toDBBook(userId), folderId).map(dbBook => Option(Book.fromDBBook(dbBook)))
          case _ => Future(None) // Folder or Book does not belong to current user
        }

      case None => Future(None)
    }

  }

  /**
   * Retrieves all books from a folder
   * @param user current user
   * @param folder parent folder
   * @return a list of Books from a folder
   */
  def retrieveAllFromFolder(user: User, folder: Folder): Future[List[Book]] = {
    folder.id match {
      case Some(folderId) => retrieveAllFromFolder(user, folderId)
      case None => Future(Nil)
    }
  }

  /**
   * Retrieves all books from a folder (by folder's id)
   * @param user current user
   * @param folderId parent folder's id
   * @return a list of Books from a folder (by folder's id)
   */
  def retrieveAllFromFolder(user: User, folderId: Long): Future[List[Book]] = {
    user.id match {
      case Some(userId) =>
        folderService.retrieveById(user, folderId).flatMap {
          case Some(_) => bookDAO.findAllInFolder(folderId).map {
            _.map(Book.fromDBBook)
          }
          case None => Future(Nil) // The folder with supplied id belongs to another user
        }

      case None => Future(Nil)
    }
  }
}
