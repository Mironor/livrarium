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

case class Token(_id: ObjectId = new ObjectId,
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

object TokenDAO extends SalatDAO[Token, ObjectId](
  collection = MongoConnection()(
    current.configuration.getString("mongodb.default.db")
      .getOrElse(throw new PlayException("Configuration error",
      "Could not find mongodb.default.db in settings"))
  )("tasks"))

object Token {
  def all(): List[Token] = TokenDAO.find(MongoDBObject.empty).toList

  def find(id: IdentityId): Option[Identity] = {
    TokenDAO.findOne(MongoDBObject(
      "identityId.userId" -> id.userId,
      "identityId.providerId" -> id.providerId
    ))
  }

  def findByEmailAndProvider(email: String, providerId: String): Option[Identity] = {
    TokenDAO.findOne(MongoDBObject("email" -> email, "identityId.providerId" -> providerId))
  }

  def save(user: Identity) = {
    TokenDAO.insert(user.asInstanceOf[Token])
    user
  }

  def delete(id: IdentityId) = {
    TokenDAO.remove(MongoDBObject(
      "identityId.userId" -> id.userId,
      "identityId.providerId" -> id.providerId
    ))
  }
}

