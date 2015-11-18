package models

import com.mohiva.play.silhouette.api.{Identity, LoginInfo}
import daos.DBTableDefinitions.{DBBook, DBFolder, DBUser}
import play.api.libs.json.{Json, Writes}

/**
 * The user object.
 * Also Identity object for Silhouette
 * @param id The unique ID of the user.
 * @param loginInfo The linked login info.
 * @param idRoot The ID of user's root folder
 * @param email Maybe the email of the authenticated provider.
 * @param avatarURL Maybe the avatar URL of the authenticated provider.
 */
case class User(id: Long,
                loginInfo: LoginInfo,
                idRoot: Long,
                email: String,
                avatarURL: Option[String]) extends Identity {

  def toDBUser: DBUser = DBUser(Option(id), idRoot, email, avatarURL)

  def owns(dbBook: DBBook) = id == dbBook.idUser

  def owns(dbFolder: DBFolder) = id == dbFolder.idUser
}

object User {
  def fromDBUser(dbUser: DBUser, loginInfo: LoginInfo): User = {
    val id = dbUser.id.getOrElse(throw new Exception(
      """A user row in the database did not have
        | an id (id field has autoincrement constraint, so it should not be null).
        | Or you are trying to cast a user's row that does not have id (this is strange)""".stripMargin
    ))

    User(id, loginInfo, dbUser.idRoot, dbUser.email, dbUser.avatarURL)
  }

  implicit val userWrites = new Writes[User] {
    def writes(user: User) = Json.obj(
      "id" -> user.id,
      "email" -> user.email,
      "idRoot" -> user.idRoot
    )
  }
}

