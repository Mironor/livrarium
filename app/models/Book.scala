package models


import com.mongodb.casbah.MongoClient
import com.mongodb.casbah.commons.Imports._
import com.novus.salat.dao._
import mongoContext._
import play.api.Play.current
import play.api.PlayException

import scala.concurrent.Future

case class Book(
                 _id: ObjectId = new ObjectId,
                 name: String = "",
                 format: List[String] = List(),
                 pages: Int = 0,
                 currentPage: Int = 0
                 )

object BookCollection extends SalatDAO[Book, ObjectId](
  collection = MongoClient()(
    current.configuration.getString("mongodb.default.db")
      .getOrElse(throw new PlayException("Configuration error",
      "Could not find mongodb.default.db in settings"))
  )("books"))

/**
 * Database layer, should be kept simple to allow easy transition to other databases if needed
 */
class BookDAO {

  def find(id: String) = Future.successful {
    BookCollection.findOne(MongoDBObject("id" -> new ObjectId(id)))
  }

  def find(id: ObjectId): Future[Option[Book]] = Future.successful {
    BookCollection.findOne(MongoDBObject("id" -> id))
  }


  def create(name: String, format: String): Option[ObjectId] = {
    BookCollection.insert(Book(
      name = name,
      format = List(format)
    ))
  }

  def save(book: Book) = {
    BookCollection.save(book)
    book
  }

  def delete(id: String) {
    BookCollection.remove(MongoDBObject("_id" -> new ObjectId(id)))
  }

}
