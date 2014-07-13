package models

import com.mohiva.play.silhouette.core.{Identity, LoginInfo}
import com.mongodb.casbah.Imports._
import com.novus.salat.dao.SalatDAO
import mongoContext._
import org.bson.types.ObjectId
import play.api.Play.current

import scala.concurrent.Future

/**
 * The user object.
 *
 * @param userID The unique ID of the user.
 * @param loginInfo The linked login info.
 * @param email Maybe the email of the authenticated provider.
 * @param avatarURL Maybe the avatar URL of the authenticated provider.
 */
case class User(userID: ObjectId = new ObjectId(),
                loginInfo: LoginInfo,
                email: Option[String],
                avatarURL: Option[String]) extends Identity

object UserCollection extends SalatDAO[User, ObjectId](
  collection = MongoClient()(
    current.configuration.getString("mongodb.default.db").get
  )("users"))

/**
 * Give access to the user object.
 */
class UserDAO {

  /**
   * Finds a user by its login info.
   *
   * @param loginInfo The login info of the user to find.
   * @return The found user or None if no user for the given login info could be found.
   */
  def find(loginInfo: LoginInfo) = Future.successful(

    UserCollection.findOne($and(
      "loginInfo.providerID" -> loginInfo.providerID,
      "loginInfo.providerKey" -> loginInfo.providerKey
    ))
  )

  /**
   * Finds a user by its user ID.
   *
   * @param userID The ID of the user to find.
   * @return The found user or None if no user for the given ID could be found.
   */
  def find(userID: ObjectId) = Future.successful(UserCollection.findOneById(userID))

  /**
   * Saves a user.
   *
   * @param user The user to save.
   * @return The saved user.
   */

  def save(user: User) = {
    UserCollection.save(user)
    Future.successful(user)
  }
}
