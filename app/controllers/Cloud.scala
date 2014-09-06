package controllers

import java.io.File

import com.mohiva.play.silhouette.contrib.services.CachedCookieAuthenticator
import com.mohiva.play.silhouette.core.providers.Credentials
import com.mohiva.play.silhouette.core.{Environment, Silhouette}
import com.sksamuel.scrimage.{Image, Format => ImgFormat}
import helpers.PDFHelper
import models.{BookDAO, Folder, User}
import play.api.Play
import play.api.libs.Files
import play.api.mvc._
import scaldi.{Injectable, Injector}
import services.RootFolderService
import play.api.libs.json._
import play.api.libs.functional.syntax._

import scala.concurrent.Future

class Cloud(implicit inj: Injector)
  extends Silhouette[User, CachedCookieAuthenticator] with Injectable {

  implicit val env = inject[Environment[User, CachedCookieAuthenticator]]
  val rootFolderService = inject[RootFolderService]
  val applicationController = inject[Application]


  implicit val folderReads: Reads[Folder] = (
    (__ \ "label").read[String] and
      (__ \ "children").read[List[Folder]]
    )(Folder.apply _)

  implicit val folderWrites: Writes[Folder] = (
    (__ \ "label").write[String] and
      (__ \ "children").write[List[Folder]]
    )(unlift(Folder.unapply))

  def index = UserAwareAction.async { implicit request =>
    request.identity match {
      case Some(user) => Future.successful(Ok(views.html.index()))

      case None => applicationController.authenticateUser(Credentials("meanor89@gmail.com", "aaaaaa"))
    }
  }

  def folders = UserAwareAction.async { implicit request =>
    request.identity match {
      case Some(user) =>
        val rootFolder = rootFolderService.retrieve(user).get
        val json = Json.toJson(rootFolder.children)
        Future.successful(Ok(json))

      case None => Future.successful(Ok(views.html.index()))
    }
  }

  def updateFolders() = UserAwareAction.async(BodyParsers.parse.json) { implicit request =>
    request.identity match {
      case Some(user) =>
        val foldersJson = request.body.validate[List[Folder]]
        foldersJson match {
          case folders: JsSuccess[List[Folder]] =>
            rootFolderService.save(folders.get, user)
            Future.successful(Ok(Json.obj()))

          case errors: JsError => Future.successful(BadRequest(Json.obj(
            "code" -> inject[Int](identified by "errors.auth.loginPasswordNotValid"),
            "fields" -> JsError.toFlatJson(errors)
          )))
        }

      case None => Future.successful(Ok(views.html.index()))
    }
  }

  def upload = UserAwareAction.async(parse.multipartFormData) { implicit request =>
    request.identity match {
      case Some(user) => uploadBook(request)
      case None => Future.successful(Ok(views.html.index()))
    }
  }

  private def uploadBook(request: RequestWithUser[MultipartFormData[Files.TemporaryFile]]) = {
    Future.successful {
      request.body.file("books").map {
        book =>
          val filename = book.filename
          val name = filename.dropRight(filename.length - filename.lastIndexOf('.'))
          val extension = filename.drop(filename.lastIndexOf('.'))
          val fileType = book.contentType.getOrElse("")
          val applicationPath = Play.current.path
          val uploadFolder = applicationPath + inject[String](identified by "folders.uploadFolder")
          val generatedImageFolder = applicationPath + inject[String](identified by "folders.generatedImageFolder")

          val idOption = BookDAO.create(name, fileType)

          idOption match {
            case Some(id) =>
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


              Ok(Json.obj(
                "id" -> id.toString,
                "name" -> name
              ))

            case None =>
              BadRequest(Json.obj("code" -> inject[Int](identified by "errors.database.noId")))
          }


      }.getOrElse {
        BadRequest(Json.obj("code" -> inject[Int](identified by "errors.upload.noFileFound")))
      }

    }
  }
}
