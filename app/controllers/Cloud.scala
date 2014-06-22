package controllers

import play.api.mvc._
import java.io.File

import models.Book
import play.api.libs.json.Json

object Cloud extends Controller {

  def index = TODO

  def menu = TODO

  def upload = Action(parse.multipartFormData) {
    request =>
      print(request.body.file("book"))
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
