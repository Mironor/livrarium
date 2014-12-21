package services

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.services.{AuthInfo, IdentityService}
import com.mohiva.play.silhouette.impl.providers.CommonSocialProfile
import daos.DBTableDefinitions.DBUser
import daos.UserDAO
import models.User
import play.api.libs.concurrent.Execution.Implicits._
import scaldi.{Injectable, Injector}

import scala.concurrent.Future



/**
 * UserDAO should only handle database interactions to improve database abstraction
 * All interactions with UserDAO should go through this class
 * Thus UserDAO should not know about the existence of the User case class
 * It will handle all the casting between User <> DBUser
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
      dbUser => User.fromDBUser(dbUser, loginInfo)
    })
  }

  /**
   * Saves a user with login info.
   * @param user The user to save.
   * @return The saved user.
   */
  def saveWithLoginInfo(user: User): Future[User] = {
    val dbUser = user.toDBUser
    userDAO.insertOrUpdateWithLoginInfo(dbUser, user.loginInfo)
      .map(returnedDBUser => user.copy(id = returnedDBUser.id))
  }

  /**
   * Saves the social profile for a user.
   * If a user exists for this profile then update the user, otherwise create a new user with the given profile.
   * @param profile The social profile to save.
   * @return The user for whom the profile was saved.
   */
  def save[A <: AuthInfo](profile: CommonSocialProfile): Future[User] = {
    val userPromise = retrieve(profile.loginInfo)

    userPromise.flatMap {
      case Some(user) => // Update user with profile
        userDAO.insertOrUpdate(DBUser(
          user.id,
          profile.email,
          profile.avatarURL
        )).map(dbUser => User.fromDBUser(dbUser, profile.loginInfo))
      case None => // Insert a new user
        userDAO.insertOrUpdateWithLoginInfo(DBUser(
          None,
          profile.email,
          profile.avatarURL
        ), profile.loginInfo).map(dbUser => User.fromDBUser(dbUser, profile.loginInfo))
    }

  }
}
