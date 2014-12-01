package controllers

//import java.io.File

import com.mohiva.play.silhouette.api.{Environment, Silhouette}
import com.mohiva.play.silhouette.impl.authenticators.SessionAuthenticator
import com.mohiva.play.silhouette.impl.providers.Credentials
//import com.sksamuel.scrimage.{Image, Format => ImgFormat}
//import helpers.{BookFormatHelper, PDFHelper}
//import play.api.Play
//import play.api.libs.Files
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.mvc._
import scaldi.{Injectable, Injector}
import services.{Folder, FolderService, User}

import scala.concurrent.Future
import scala.language.postfixOps

class Cloud(implicit inj: Injector)
  extends Silhouette[User, SessionAuthenticator] with Injectable {

  implicit val env = inject[Environment[User, SessionAuthenticator]]

  val applicationController = inject[Application]
  val folderService = inject[FolderService]

  /** Case classes used in requests **/
  case class FolderContents(id: Long,
                            folders: List[Folder])


  /** Readers and Writers to handle json requests
    * https://www.playframework.com/documentation/2.1.0/ScalaJsonRequests
    */
  implicit val appendFolderReads = (
    (__ \ 'idParent).read[Long] and
      (__ \ 'name).read[String]
    ) tupled

  implicit val folderWrites: Writes[Folder] = (
    (__ \ "id").write[Option[Long]] and
      (__ \ "label").write[String] and
      (__ \ "children").write[List[Folder]]
    )(unlift(Folder.unapply))

  implicit val folderContentsWrites: Writes[FolderContents] = (
    (__ \ "id").write[Long] and
      (__ \ "folders").write[List[Folder]]
    )(unlift(FolderContents.unapply))


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
            folderService.appendTo(user, parentFolderId, name).map{
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

  def upload = UserAwareAction.async(parse.multipartFormData) { implicit request =>
    request.identity match {
      case Some(user) => Future.successful(Ok(views.html.index()))//uploadBook(request)
      case None => Future.successful(Ok(views.html.index()))
    }
  }

  /*
  private def uploadBook(request: RequestWithUser[MultipartFormData[Files.TemporaryFile]]) = {
    Future.successful {
      request.body.file("books").map {
        book =>
          val filename = book.filename
          val name = filename.dropRight(filename.length - filename.lastIndexOf('.'))
          val extension = filename.drop(filename.lastIndexOf('.'))

          val fileType = BookFormatHelper.normalize(
            book.contentType.getOrElse(BookFormatHelper.NONE)
          )

          val applicationPath = Play.current.path
          val uploadFolder = applicationPath + inject[String](identified by "folders.uploadFolder")
          val generatedImageFolder = applicationPath + inject[String](identified by "folders.generatedImageFolder")

          val id = "ss"

          val uploadedBookPath = s"$uploadFolder/$id$extension"
          book.ref.moveTo(new File(uploadedBookPath))

          val uploadedBookImagePath = s"$generatedImageFolder/$id.jpg"
          PDFHelper.extractImageFromPdf(uploadedBookPath, uploadedBookImagePath)

          val uploadedBookSmallThumbPath = s"$generatedImageFolder/$id-small.jpg"
          val uploadedImage = new File(uploadedBookImagePath)

          Image(uploadedImage).fit(
            inject[Int](identified by "books.thumbnailWidth"),
            inject[Int](identified by "books.thumbnailHeight")
          ).write(new File(uploadedBookImagePath), ImgFormat.JPEG)

          Image(uploadedImage).fit(
            inject[Int](identified by "books.smallThumbnailWidth"),
            inject[Int](identified by "books.smallThumbnailHeight")
          ).write(new File(uploadedBookSmallThumbPath), ImgFormat.JPEG)

          val bookModel = Book(
            _id = id,
            name = name,
            format = List(fileType),
            pages = PDFHelper.getTotalPages(uploadedBookPath)
          )

          bookService.save(bookModel)
          Ok(Json.obj(
            "id" -> id.toString,
            "name" -> name
          ))

      }.getOrElse {
        BadRequest(Json.obj("code" -> inject[Int](identified by "errors.upload.noFileFound")))
      }

    }

  }
  */

}
