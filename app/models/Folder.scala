package models

import com.mongodb.casbah.MongoClient
import com.mongodb.casbah.Imports._
import com.novus.salat.dao._
import mongoContext._
import play.api.Play.current
import play.api.PlayException

case class Folder(_id: ObjectId = new ObjectId, label: String, children: List[Folder])

object FolderDAO extends SalatDAO[Folder, ObjectId](
  collection = MongoClient()(
    current.configuration.getString("mongodb.default.db")
      .getOrElse(throw new PlayException("Configuration error",
      "Could not find mongodb.default.db in settings"))
  )("folders"))

object Folder {


  def all(): List[Folder] = FolderDAO.find(MongoDBObject.empty).toList

  def find(id: String) = FolderDAO.findOneById(new ObjectId(id))

  def create(folder: Folder): Option[ObjectId] = {
    FolderDAO.insert(folder)
  }

  def delete(id: String) {
    FolderDAO.remove(MongoDBObject("_id" -> new ObjectId(id)))
  }

}
