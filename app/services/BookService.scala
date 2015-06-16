package services

import daos.BookDAO
import daos.DBTableDefinitions.DBBook
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
   * @return a promise of a list of user's books
   */
  def retrieveAll(user: User): Future[Seq[Book]] = {
    bookDAO.findAll(user.id).map {
      _.map(Book.fromDBBook)
    }
  }

  /**
   * Retrieves book by id (with user checking)
   * @param user current user, book's owner
   * @param bookId the id of the book to retrieve
   * @return a promise of a book (None if no book was found)
   */
  def retrieve(user: User, bookId: Long): Future[Option[Book]] = {
    bookDAO.findById(bookId).map {
      _.filter(user.owns).map(Book.fromDBBook)
    }
  }

  /**
   * Creates new book from supplied info
   * @param user user to which the book will belong
   * @param identifier future's book identifier
   * @param name future book's name
   * @param format future book's format
   * @return a promise of a created book
   */
  def create(user: User, identifier: String, name: String, format: String, totalPages: Int = 0): Future[Book] = {
    val dbBook = DBBook(None, user.id, identifier, name, format, totalPages)
    bookDAO.insert(dbBook).map(Book.fromDBBook)
  }

  /**
   * Saves supplied book for the user
   * Checks if trying to update another user's book (returns Future(None) in this case)
   * @param user current user, book's owner
   * @param book book to save
   * @return a promise of the updated book (None if could not save the book)
   */
  def save(user: User, book: Book): Future[Option[Book]] = {
    retrieve(user, book.id).flatMap {
      case Some(_) =>
        bookDAO.update(book.toDBBook(user.id))
          .map(Book.fromDBBook)
          .map(Option.apply)

      case None => Future(None) // The book with supplied id belongs to another user
    }
  }

  /**
   * Adds book to a supplied folder
   * @param user current user book's and folder's owner
   * @param book book to add
   * @param folder parent folder (should be owned by current user)
   * @return a promise of the added book (None if could not add the book to the folder)
   */
  def addToFolder(user: User, book: Book, folder: Folder): Future[Option[Book]] = {
    addToFolder(user, book, folder.id)
  }

  /**
   * Adds book to a supplied folder's id
   * @param user current user book's and folder's owner
   * @param book book to add
   * @param folderId parent folder's id (should be owned by current user)
   * @return a promise of the added book (None if could not add the book to the folder)
   */
  def addToFolder(user: User, book: Book, folderId: Long): Future[Option[Book]] = {
    addToFolder(user, book.id, folderId)
  }

  /**
   * Adds book (by id) to the folder (also by id)
   * @param user current user book's and folder's owner
   * @param bookId id of the book that is to add to the folder
   * @param folderId  parent folder's id (should be owned by current user)
   * @return a promise of the added book (None if could not add the book to the folder)
   */
  def addToFolder(user: User, bookId: Long, folderId: Long): Future[Option[Book]] = {
    val book = retrieve(user, bookId)
    val folder = folderService.retrieve(user, folderId)

    book zip folder flatMap {
      case (Some(retrievedBook), Some(_)) =>
        bookDAO.relateBookToFolder(retrievedBook.id, folderId)
          .map(_ => Option(retrievedBook))

      case _ => Future.successful(None) // Folder or Book does not belong to current user
    }
  }

  /**
   * Retrieves all books from a folder
   * @param user current user, folder's owner
   * @param folder parent folder (should be owned by current user)
   * @return a promise of the books contained in the supplied folder
   */
  def retrieveAllFromFolder(user: User, folder: Folder): Future[Seq[Book]] = {
    retrieveAllFromFolder(user, folder.id)
  }

  /**
   * Retrieves all books from a folder (by folder's id)
   * @param user current user, folder's owner
   * @param folderId parent folder's id (should be owned by current user)
   * @return a promise of the books contained in the supplied folder
   */
  def retrieveAllFromFolder(user: User, folderId: Long): Future[Seq[Book]] = {
    folderService.retrieve(user, folderId).flatMap {
      case Some(_) => bookDAO.findAllInFolder(folderId).map {
        _.map(Book.fromDBBook)
      }
      case None => Future(Nil) // The folder with supplied id belongs to another user
    }
  }

}
