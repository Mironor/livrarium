package models

import play.api.Play.current
import play.api.PlayException

import mongoContext._

import com.novus.salat.dao._

import com.mongodb.casbah.commons.Imports._
import com.mongodb.casbah.MongoConnection

import securesocial.core._
import securesocial.core.IdentityId
import org.bson.types.ObjectId

case class User(_id: ObjectId = new ObjectId, identity: SocialUser)

object UserDAO extends SalatDAO[User, ObjectId](
  collection = MongoConnection()(
    current.configuration.getString("mongodb.default.db")
      .getOrElse(throw new PlayException("Configuration error",
      "Could not find mongodb.default.db in settings"))
  )("users"))

object User {
  def all(): List[User] = UserDAO.find(MongoDBObject.empty).toList

  def find(id: IdentityId): Option[User] = {
    UserDAO.findOne(MongoDBObject(
      "identity.identityId.userId" -> id.userId,
      "identity.identityId.providerId" -> id.providerId
    ))
  }

  def findByEmailAndProvider(email: String, providerId: String): Option[User] = {
    UserDAO.findOne(MongoDBObject("identity.email" -> email, "identity.identityId.providerId" -> providerId))
  }

  def save(user: User) = {
    UserDAO.insert(user)
  }

  def delete(id: IdentityId) = {
    UserDAO.remove(MongoDBObject(
      "identity.identityId.userId" -> id.userId,
      "identity.identityId.providerId" -> id.providerId
    ))
  }
}
