package controllers

import java.io.File

import com.mohiva.play.silhouette.contrib.services.CachedCookieAuthenticator
import com.mohiva.play.silhouette.core.{Environment, Silhouette}
import models.{Folder, RootFolderDAO, Book, User}
import play.api.libs.json.{JsError, JsSuccess, Json}
import play.api.mvc._
import scaldi.{Injectable, Injector}
import services.RootFolderService

import scala.concurrent.Future

class Cloud(implicit inj: Injector)
  extends Silhouette[User, CachedCookieAuthenticator] with Injectable {

  implicit val env = inject[Environment[User, CachedCookieAuthenticator]]
  val rootFolderService = inject[RootFolderService]

  def index = UserAwareAction.async { implicit request =>
    request.identity match {
      case Some(user) => Future.successful(Ok(views.html.index()))
      case None => Future.successful(Ok(views.html.index()))
      //      case None => Future.successful(Redirect(routes.Application.index()))
    }
  }

  def folders = UserAwareAction.async { implicit request =>
    request.identity match {
      case Some(user) =>
        val folders = rootFolderService.retrieve(user)
        val json = Json.toJson(folders)
        Future.successful(Ok(json))

      case None => Future.successful(Ok(views.html.index()))
    }
  }

  def updateFolders() = UserAwareAction.async(BodyParsers.parse.json) { implicit request =>
    request.identity match {
      case Some(user) =>
        val foldersJson = request.body.validate[Folder]
        foldersJson match {
          case folders: JsSuccess[Folder] =>
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
