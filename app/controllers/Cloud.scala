package controllers

import java.io.File

import com.mohiva.play.silhouette.contrib.services.CachedCookieAuthenticator
import com.mohiva.play.silhouette.core.{Environment, Silhouette}
import models.{Book, User}
import play.api.libs.json.Json
import play.api.mvc._
import scaldi.{Injectable, Injector}

import scala.concurrent.Future

class Cloud(implicit inj: Injector)
  extends Silhouette[User, CachedCookieAuthenticator] with Injectable {

  implicit val env = inject[Environment[User, CachedCookieAuthenticator]]

  def index = UserAwareAction.async { implicit request =>
    request.identity match {
      case Some(user) => Future.successful(Ok(views.html.index()))
//      case None => Future.successful(Redirect(routes.Application.index()))
      case None => Future.successful(Ok(views.html.index()))
    }
  }

  def menu = TODO

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
