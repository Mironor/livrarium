package services

import com.mohiva.play.silhouette.core.LoginInfo
import com.mohiva.play.silhouette.core.providers.CommonSocialProfile
import com.mohiva.play.silhouette.core.services.{AuthInfo, IdentityService}
import models.{UserDAO, User}
import play.api.libs.concurrent.Execution.Implicits._
import scaldi.{Injectable, Injector}

import scala.concurrent.Future

/**
 * Handles actions to users.
 * UserDAO should only handle database interactions to improve database abstraction
 */
class UserService(implicit inj: Injector)  extends IdentityService[User] with Injectable {
  val userDAO  = inject[UserDAO]

  /**
   * Retrieves a user that matches the specified login info.
   *
   * @param loginInfo The login info to retrieve a user.
   * @return The retrieved user or None if no user could be retrieved for the given login info.
   */
  def retrieve(loginInfo: LoginInfo): Future[Option[User]] = userDAO.find(loginInfo)

  /**
   * Saves a user.
   *
   * @param user The user to save.
   * @return The saved user.
   */
  def save(user: User) = userDAO.save(user)

  /**
   * Saves the social profile for a user.
   *
   * If a user exists for this profile then update the user, otherwise create a new user with the given profile.
   *
   * @param profile The social profile to save.
   * @return The user for whom the profile was saved.
   */
  def save[A <: AuthInfo](profile: CommonSocialProfile[A]) = {
    userDAO.find(profile.loginInfo).flatMap {
      case Some(user) => // Update user with profile
        userDAO.save(user.copy(
          email = profile.email,
          avatarURL = profile.avatarURL
        ))
      case None => // Insert a new user
        userDAO.save(User(
          id = None,
          loginInfo = profile.loginInfo,
          email = profile.email,
          avatarURL = profile.avatarURL
        ))
    }
  }
}
