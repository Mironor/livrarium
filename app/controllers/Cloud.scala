package controllers

import java.io.File

import com.mohiva.play.silhouette.contrib.services.CachedCookieAuthenticator
import com.mohiva.play.silhouette.core.providers.Credentials
import com.mohiva.play.silhouette.core.{Environment, Silhouette}
import models.{Folder, Book, User}
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

  def upload = Action(parse.multipartFormData) {
    request =>
      request.body.file("book").map {
        book =>
          val name = book.filename
          val fileType = book.contentType.getOrElse("")

          book.ref.moveTo(new File(s"/tmp/$name"))

          Book.create(name, fileType)
          Ok(Json.obj("error" -> ""))
      }.getOrElse {
        Ok(Json.obj("error" -> "Missing file"))
      }

  }
}
