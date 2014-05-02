package service

import play.api.Application
import securesocial.core._
import securesocial.core.providers.Token
import securesocial.core.IdentityId
import models.{AuthToken, Identity}
import securesocial.core.Identity

class UserService(application: Application) extends UserServicePlugin(application) {

  def find(id: IdentityId): Option[Identity] = {
    Identity.find(id)
  }

  def findByEmailAndProvider(email: String, providerId: String): Option[Identity] = {
    Identity.findByEmailAndProvider(email, providerId)
  }

  def save(identity: Identity): Identity = {
    Identity.save(identity)
    identity
  }

  def save(token: Token) {
    AuthToken.save(token)
  }

  def findToken(token: String): Option[Token] = {
    AuthToken.find(token)
  }

  def deleteToken(uuid: String) {
    AuthToken.delete(uuid)
  }

  def deleteTokens() {
    AuthToken.deleteAll()
  }

  def deleteExpiredTokens() {
    AuthToken.deleteExpired()
  }
}
