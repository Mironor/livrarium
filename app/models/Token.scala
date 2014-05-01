package models

import play.api.Play.current
import play.api.PlayException

import mongoContext._

import com.novus.salat.dao._

import com.mongodb.casbah.commons.Imports._
import com.mongodb.casbah.MongoConnection

import securesocial.core.providers.Token
import org.bson.types.ObjectId
import com.mongodb.casbah.commons.conversions.scala.{RegisterJodaTimeConversionHelpers, RegisterConversionHelpers}

object AuthTokenDAO extends SalatDAO[Token, ObjectId](
  collection = MongoConnection()(
    current.configuration.getString("mongodb.default.db")
      .getOrElse(throw new PlayException("Configuration error",
      "Could not find mongodb.default.db in settings"))
  )("auth_tokens"))

object AuthToken {
  def all(): List[Token] = AuthTokenDAO.find(MongoDBObject.empty).toList

  def find(uuid: String): Option[Token] = {
    AuthTokenDAO.findOne(MongoDBObject(
      "uuid" -> uuid
    ))
  }

  def save(token: Token) = {
    RegisterConversionHelpers()
    RegisterJodaTimeConversionHelpers()
    AuthTokenDAO.insert(token)
    token
  }

  def delete(uuid: String) = {
    AuthTokenDAO.remove(MongoDBObject(
      "uuid" -> uuid
    ))
  }
}

