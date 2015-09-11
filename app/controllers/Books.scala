package controllers

import com.mohiva.play.silhouette.api.Environment
import com.mohiva.play.silhouette.impl.authenticators.SessionAuthenticator
import helpers.{BookFormatHelper, FileHelper}
import models.{Book, Folder, User}
import play.api.i18n.MessagesApi
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.Json
import scaldi.{Injectable, Injector}
import services.{BookService, FolderService}

import scala.concurrent.Future

class Books(implicit inj: Injector)
  extends Controller with Injectable {

  implicit val messagesApi = inject[MessagesApi]
  implicit val env = inject[Environment[User, SessionAuthenticator]]

  private val fileHelper = inject[FileHelper]

  private val folderService = inject[FolderService]
  private val bookService = inject[BookService]

  /**
   * Streams book to user
   * @param folderId folder in which the book is situated
   * @param identifier book's identifier
   * @param extension book's extension
   * @return
   */
  def stream(folderId: Long, identifier: String, extension: String) = authenticatedActionAsync { user =>
    // Fetching is necessary to verify that user is the real owner of folder / book
    fetchFolderBook(user, folderId, identifier).map {
      case (Some(folder), Some(book)) =>
        extension match {
          case BookFormatHelper.PDF =>
            val file = fileHelper.getUploadedFile(folder.id, book.identifier, extension)
            Ok.sendFile(file)

          case _ => BadRequest(Json.obj(
            "code" -> inject[Int](identified by "errors.cloud.fileTypeNotSupported"),
            "message" -> s"Book format $extension is currently unsupported"
          ))
        }
      case _ => Unauthorized
    }
  }

  private def fetchFolderBook(user: User, folderId: Long, identifier: String): Future[(Option[Folder], Option[Book])] = {
    val folder = folderService.retrieve(user, folderId)
    val book = bookService.retrieve(user, identifier)

    folder zip book
  }


  //  def index = v2.TODO
  //  def info(id: String) = v2.TODO
  //  def read(id: String) = v2.TODO
  //  def move(id: String) = v2.TODO
  //  def toRead(id: String) = v2.TODO
  //  def update(id: String) = v2.TODO
  //  def archive(id: String) = TODO
  //  def delete(id: String) = TODO

}
