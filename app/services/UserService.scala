package services

import com.mohiva.play.silhouette.core.{Identity, LoginInfo}
import com.mohiva.play.silhouette.core.providers.CommonSocialProfile
import com.mohiva.play.silhouette.core.services.{AuthInfo, IdentityService}
import models.DBTableDefinitions.DBUser
import models.UserDAO
import play.api.libs.concurrent.Execution.Implicits._
import scaldi.{Injectable, Injector}

import scala.concurrent.Future

/**
 * The user object.
 *
 * @param id The unique ID of the user.
 * @param loginInfo The linked login info.
 * @param email Maybe the email of the authenticated provider.
 * @param avatarURL Maybe the avatar URL of the authenticated provider.
 */
case class User(id: Option[Long],
                loginInfo: LoginInfo,
                email: Option[String],
                avatarURL: Option[String]) extends Identity


/**
 * Handles actions to users.
 * UserDAO should only handle database interactions to improve database abstraction
 * Thus UserDAO should not know about User case class existance, instead this service
 * will handle all the casting between User <> DBUser
 */
class UserService(implicit inj: Injector) extends IdentityService[User] with Injectable {
  val userDAO = inject[UserDAO]

  /**
   * Retrieves a user that matches the specified login info.
   *
   * @param loginInfo The login info to retrieve a user.
   * @return The retrieved user or None if no user could be retrieved for the given login info.
   */
  def retrieve(loginInfo: LoginInfo): Future[Option[User]] = {
    val dbUserPromise = userDAO.find(loginInfo)
    dbUserPromise.map(dbUserOption => dbUserOption.map {
      dbUser => User(dbUser.id, loginInfo, dbUser.email, dbUser.avatarURL)
    })
  }

  /**
   * Saves a user.
   *
   * @param user The user to save.
   * @return The saved user.
   */
  def saveWithLoginInfo(user: User): Future[User] = {
    val dbUser = DBUser(user.id, user.email, user.avatarURL)
    userDAO.saveWithLoginInfo(dbUser, user.loginInfo)
      .map(returnedDBUser => user.copy(id = returnedDBUser.id))
  }

  /**
   * Saves the social profile for a user.
   *
   * If a user exists for this profile then update the user, otherwise create a new user with the given profile.
   *
   * @param profile The social profile to save.
   * @return The user for whom the profile was saved.
   */
  def save[A <: AuthInfo](profile: CommonSocialProfile[A]): Future[User] = {
    val userPromise = retrieve(profile.loginInfo)

    userPromise.flatMap {
      case Some(user) => // Update user with profile
        userDAO.save(DBUser(
          user.id,
          profile.email,
          profile.avatarURL
        )).map(dbUser => User(dbUser.id, profile.loginInfo, dbUser.email, dbUser.avatarURL))
      case None => // Insert a new user
        userDAO.saveWithLoginInfo(DBUser(
          None,
          profile.email,
          profile.avatarURL
        ), profile.loginInfo).map(dbUser => User(dbUser.id, profile.loginInfo, dbUser.email, dbUser.avatarURL))
    }

  }

  /**
   * Hack for silhouette
   */
  def saveNoAsync[A <: AuthInfo](profile: CommonSocialProfile[A]) = {

  }
}
