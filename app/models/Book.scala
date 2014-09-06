package models


import com.mongodb.casbah.MongoClient
import com.mongodb.casbah.commons.Imports._
import com.novus.salat.dao._
import mongoContext._
import play.api.Play.current
import play.api.PlayException

case class Book(_id: ObjectId = new ObjectId, name: String, fileType: String)

object BookCollection extends SalatDAO[Book, ObjectId](
  collection = MongoClient()(
    current.configuration.getString("mongodb.default.db")
      .getOrElse(throw new PlayException("Configuration error",
      "Could not find mongodb.default.db in settings"))
  )("books"))

object BookDAO {

  def create(name: String, fileType: String): Option[ObjectId] = {
    BookCollection.insert(Book(name = name, fileType = fileType))
  }

  def delete(id: String) {
    BookCollection.remove(MongoDBObject("_id" -> new ObjectId(id)))
  }

}
