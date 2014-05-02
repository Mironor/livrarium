package models

import play.api.Play.current
import play.api.PlayException

import mongoContext._

import com.novus.salat.dao._

import com.mongodb.casbah.Imports._
import com.mongodb.casbah.MongoConnection

import securesocial.core._
import securesocial.core.IdentityId
import org.bson.types.ObjectId

object IdentityDAO extends SalatDAO[SocialUser, ObjectId](
  collection = MongoConnection()(
    current.configuration.getString("mongodb.default.db")
      .getOrElse(throw new PlayException("Configuration error",
      "Could not find mongodb.default.db in settings"))
  )("identities"))

object Identity {
  def find(id: IdentityId): Option[SocialUser] = {
    IdentityDAO.findOne($and(
      "identityId.userId" -> id.userId,
      "identityId.providerId" -> id.providerId
    ))
  }

  def findByEmailAndProvider(email: String, providerId: String): Option[SocialUser] = {
    IdentityDAO.findOne($and(
      "email" -> email,
      "identityId.providerId" -> providerId
    ))
  }

  def save(identity: Identity) = {
    IdentityDAO.save(identity.asInstanceOf[SocialUser])
  }

  def delete(id: IdentityId) = {
    IdentityDAO.remove($and(
      "identityId.userId" -> id.userId,
      "identityId.providerId" -> id.providerId
    ))
  }
}
