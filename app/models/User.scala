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

case class User(_id: ObjectId = new ObjectId,
                identityId: IdentityId,
                firstName: String,
                lastName: String,
                fullName: String,
                email: Option[String],
                avatarUrl: Option[String],
                authMethod: AuthenticationMethod,
                oAuth1Info: Option[OAuth1Info],
                oAuth2Info: Option[OAuth2Info],
                passwordInfo: Option[PasswordInfo]) extends Identity {}

object UserDAO extends SalatDAO[User, ObjectId](
  collection = MongoConnection()(
    current.configuration.getString("mongodb.default.db")
      .getOrElse(throw new PlayException("Configuration error",
      "Could not find mongodb.default.db in settings"))
  )("tasks"))

object User {
  def all(): List[User] = UserDAO.find(MongoDBObject.empty).toList

  def find(id: IdentityId): Option[Identity] = {
    UserDAO.findOne(MongoDBObject(
      "identityId.userId" -> id.userId,
      "identityId.providerId" -> id.providerId
    ))
  }

  def findByEmailAndProvider(email: String, providerId: String): Option[Identity] = {
    UserDAO.findOne(MongoDBObject("email" -> email, "identityId.providerId" -> providerId))
  }

  def save(user: Identity) = {
    UserDAO.insert(user.asInstanceOf[User])
    user
  }

  def delete(id: IdentityId) = {
    UserDAO.remove(MongoDBObject(
      "identityId.userId" -> id.userId,
      "identityId.providerId" -> id.providerId
    ))
  }
}
