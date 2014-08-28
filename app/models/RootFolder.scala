package models

import com.mongodb.casbah.MongoClient
import com.mongodb.casbah.Imports._
import com.novus.salat.dao._
import mongoContext._
import play.api.Play.current

import scala.concurrent.Future

/**
 * A wrapper that takes care of user's id
 * as we don't want each folder to store user's id, just the root one
 */
case class RootFolder(id: ObjectId = new ObjectId, userId: ObjectId = new ObjectId, children: List[Folder])

case class Folder(label: String, children: List[Folder])




/**
 * Collection stores only root folders
 * one root folder per user
 */
object RootFolderCollection extends SalatDAO[RootFolder, ObjectId](
  collection = MongoClient()(current.configuration.getString("mongodb.default.db").get)("folders"))

class RootFolderDAO {

  def find(id: String): Option[RootFolder] = RootFolderCollection.findOneById(new ObjectId(id))

  def findByUser(user: User): Option[RootFolder] = RootFolderCollection.findOne(MongoDBObject("userId" -> user.id))
  
  def create(rootFolder: RootFolder): Option[ObjectId] = {
    RootFolderCollection.insert(rootFolder)
  }

  def save(rootFolder: RootFolder) = {
    RootFolderCollection.save(rootFolder)
    Future.successful(rootFolder)
  }

  def delete(id: String) {
    RootFolderCollection.remove(MongoDBObject("_id" -> new ObjectId(id)))
  }

}
