package models

import play.api.Play.current
import play.api.PlayException

import mongoContext._

import com.novus.salat.dao._

import com.mongodb.casbah.MongoConnection
import com.mongodb.casbah.Imports._

import securesocial.core.providers.Token
import org.bson.types.ObjectId
import com.mongodb.casbah.commons.conversions.scala.RegisterJodaTimeConversionHelpers
import com.mongodb.WriteResult
import com.mongodb.casbah.commons.MongoDBObject

object AuthTokenDAO extends SalatDAO[Token, ObjectId](
  collection = MongoConnection()(
    current.configuration.getString("mongodb.default.db")
      .getOrElse(throw new PlayException("Configuration error",
      "Could not find mongodb.default.db in settings"))
  )("auth_tokens"))

object AuthToken {
  def all(): List[Token] = {
    AuthTokenDAO.find(MongoDBObject.empty).toList
  }

  def find(uuid: String): Option[Token] = {
    AuthTokenDAO.findOne("uuid" $eq uuid)
  }

  def save(token: Token) = {
    RegisterJodaTimeConversionHelpers()
    AuthTokenDAO.save(token)
  }

  def delete(uuid: String) = {
    AuthTokenDAO.remove("uuid" $eq uuid)
  }

  def delete(token: Token): WriteResult = {
    delete(token.uuid)
  }

  def deleteAll() = {
    AuthTokenDAO.remove(MongoDBObject.empty)
  }

  def deleteExpired() = {
    val tokenUuids = AuthToken.all().filter(_.isExpired).map(_.uuid)
    AuthTokenDAO.remove("uuid" $in tokenUuids)
  }
}

