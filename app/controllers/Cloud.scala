package controllers

//import java.io.File

import java.io.File

import com.mohiva.play.silhouette.api.util.Credentials
import com.mohiva.play.silhouette.api.{Environment, Silhouette}
import com.mohiva.play.silhouette.impl.authenticators.SessionAuthenticator
import com.sksamuel.scrimage.{Format => ImgFormat, Image}
import helpers.{PDFHelper, RandomIdGenerator, BookFormatHelper}
import org.apache.commons.io.FileUtils
import play.api.Play
import play.api.libs.Files
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.mvc._
import scaldi.{Injectable, Injector}
import services._

import scala.concurrent.Future
import scala.language.postfixOps

class Cloud(implicit inj: Injector)
  extends Silhouette[User, SessionAuthenticator] with Injectable {

  implicit val env = inject[Environment[User, SessionAuthenticator]]

  val applicationController = inject[Application]

  val folderService = inject[FolderService]
  val bookService = inject[BookService]

  val randomIdGenerator = inject[RandomIdGenerator]


  /** Readers and Writers to handle json requests
    * https://www.playframework.com/documentation/2.1.0/ScalaJsonRequests
    */
  implicit val appendFolderReads = (
    (__ \ 'idParent).read[Long] and
      (__ \ 'name).read[String]
    ) tupled

  implicit val uploadBookReads =  (__ \ 'idFolder).read[Long]


  def index = UserAwareAction.async { implicit request =>
    request.identity match {
      case Some(user) => Future.successful(Ok(views.html.index()))

      case None => applicationController.authenticateUser(Credentials("meanor@gmail.com", "aaaaaa"))
    }
  }

  def getRootContent = UserAwareAction.async { implicit request =>
    request.identity match {
      case Some(user) => folderService.retrieveRoot(user).flatMap { rootFolderOption =>
        val rootFolder = rootFolderOption.getOrElse(throw new Exception("Root folder not found"))
        val rootFolderId = rootFolder.id.getOrElse(throw new Exception("Root folder does not have id"))
        val rootFolderContentPromise = getFolderContents(user, rootFolderId)

        rootFolderContentPromise.map(rootContent => Ok(Json.toJson(rootContent)))
      }

      case None => Future.successful(Ok(views.html.index()))
    }
  }

  private def getFolderContents(user: User, folderId: Long): Future[FolderContents] = {
    val childrenPromise = folderService.retrieveChildren(user, folderId)
    childrenPromise.map(children => FolderContents(folderId, children))
  }

  def getContent(id: Long) = UserAwareAction.async { implicit request =>
    request.identity match {
      case Some(user) =>
        val folderContentsPromise = getFolderContents(user, id)
        folderContentsPromise.map(rootContent => Ok(Json.toJson(rootContent)))

      case None => Future.successful(Ok(views.html.index()))
    }
  }

  def createFolder() = UserAwareAction.async(BodyParsers.parse.json) { implicit request =>
    request.identity match {
      case Some(user) =>
        request.body.validate[(Long, String)].map {
          case (parentFolderId: Long, name: String) =>
            folderService.appendTo(user, parentFolderId, name).map {
              folder => Ok(Json.toJson(folder))
            }
        }.recoverTotal {
          error => Future.successful(BadRequest(Json.obj(
            "code" -> inject[Int](identified by "errors.request.badRequest"),
            "fields" -> JsError.toFlatJson(error)
          )))
        }

      case None => Future.successful(Ok(views.html.index()))
    }
  }

  def upload(uploadFolderId: Long) = UserAwareAction.async(parse.multipartFormData) { implicit request =>
    request.identity match {
      case Some(user) => uploadBook(user, uploadFolderId)
      case None => Future.successful(Ok(views.html.index()))
    }
  }

  private def uploadBook(user: User, uploadFolderId: Long)(implicit request: UserAwareRequest[MultipartFormData[Files.TemporaryFile]]) = {
    val uploadInputName = inject[String](identified by "books.uploadInputName")
    request.body.file(uploadInputName).map {
      book =>
        val filename = book.filename
        val name = filename.dropRight(filename.length - filename.lastIndexOf('.'))
        val extension = filename.drop(filename.lastIndexOf('.'))
        val uuid = randomIdGenerator.generateBookId()

        val applicationPath = Play.current.path
        val userId = user.id.getOrElse(throw new Exception("User's id is not defined"))
        val uploadFolder = applicationPath + inject[String](identified by "folders.uploadPath")
        val generatedImageFolder = applicationPath + inject[String](identified by "folders.generatedImagePath")

        val userUploadFolder = s"$uploadFolder/$userId"
        val userGeneratedImageFolder = s"$generatedImageFolder/$userId"

        FileUtils.forceMkdir(new File(userUploadFolder))

        val uploadedBookPath = s"$userUploadFolder/$uuid$extension"
        book.ref.moveTo(new File(uploadedBookPath))

        FileUtils.forceMkdir(new File(userGeneratedImageFolder))

        val generatedThumbnailPath = s"$userGeneratedImageFolder/$uuid.jpg"
        PDFHelper.extractImageFromPdf(uploadedBookPath, generatedThumbnailPath)

        val generatedThumbnail = new File(generatedThumbnailPath)

        Image(generatedThumbnail).fit(
          inject[Int](identified by "books.thumbnailWidth"),
          inject[Int](identified by "books.thumbnailHeight")
        ).write(new File(generatedThumbnailPath), ImgFormat.JPEG)

        val generatedSmallThumbnailPath = s"$generatedImageFolder/$userId/$uuid-small.jpg"

        Image(generatedThumbnail).fit(
          inject[Int](identified by "books.smallThumbnailWidth"),
          inject[Int](identified by "books.smallThumbnailHeight")
        ).write(new File(generatedSmallThumbnailPath), ImgFormat.JPEG)

        val fileType = BookFormatHelper.normalize(
          book.contentType.getOrElse(BookFormatHelper.NONE)
        )

        val totalPages = PDFHelper.getTotalPages(uploadedBookPath)

        val bookModel = Book(
          None,
          uuid,
          name,
          fileType,
          totalPages
        )

        val savedBookPromise = bookService.save(user, bookModel)

        savedBookPromise.map { book =>
          val bookId = book.id.getOrElse(throw new Exception("Book's id is not defined"))
          Ok(Json.obj(
            "id" -> bookId,
            "uuid" -> book.identifier,
            "name" -> name
          ))
        }

    }.getOrElse {
      Future.successful(BadRequest(
        Json.obj("code" -> inject[Int](identified by "errors.upload.noFileFound"))
      ))
    }


  }

}
