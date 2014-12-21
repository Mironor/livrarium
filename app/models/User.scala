package models

import com.mohiva.play.silhouette.api.{Identity, LoginInfo}
import daos.DBTableDefinitions.DBUser

/**
 * The user object.
 * Also Identity object for Silhouette
 * @param id The unique ID of the user.
 * @param loginInfo The linked login info.
 * @param email Maybe the email of the authenticated provider.
 * @param avatarURL Maybe the avatar URL of the authenticated provider.
 */
case class User(id: Option[Long],
                loginInfo: LoginInfo,
                email: Option[String],
                avatarURL: Option[String]) extends Identity {

  def toDBUser: DBUser =  DBUser(id, email, avatarURL)
}

object User {
  def fromDBUser(dbUser: DBUser, loginInfo: LoginInfo): User = User(dbUser.id, loginInfo, dbUser.email, dbUser.avatarURL)
}

