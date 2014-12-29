package services

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.services.IdentityService
import daos.UserDAO
import daos.silhouette.LoginInfoDAO
import models.User
import play.api.libs.concurrent.Execution.Implicits._
import scaldi.{Injectable, Injector}

import scala.concurrent.Future

class UserService(implicit inj: Injector) extends IdentityService[User] with Injectable {
  val userDAO = inject[UserDAO]
  val loginInfoDAO = inject[LoginInfoDAO]

  /**
   * Retrieves a user that matches the specified login info.
   * @param loginInfo login info to retrieve a user
   * @return the retrieved user (None if the user could not be found)
   */
  def retrieve(loginInfo: LoginInfo): Future[Option[User]] = {
    val dbUserPromise = userDAO.findByLoginInfo(loginInfo)
    dbUserPromise.map(dbUserOption => dbUserOption.map {
      dbUser => User.fromDBUser(dbUser, loginInfo)
    })
  }

  /**
   * Saves a user (with its login info).
   * @param user The user to save.
   * @return The saved user (None if user was new and he/she was not inserted).
   */
  def save(user: User): Future[Option[User]] = {
    saveUser(user).flatMap { savedUser =>
      savedUser.id match {
        case Some(userId) => saveLoginInfo(user.loginInfo, userId).map(_ => Option(savedUser))
        case None => Future.successful(None) // user was not inserted, thus not inserting the login info
      }
    }
  }

  private def saveUser(user: User): Future[User] = {
    user.id match {
      case Some(_) => userDAO.update(user.toDBUser).map(x => user.copy(id = x.id))
      case None => userDAO.insert(user.toDBUser).map(x => user.copy(id = x.id))
    }
  }

  private def saveLoginInfo(loginInfo: LoginInfo, userId: Long): Future[_] = {
    loginInfoDAO.find(loginInfo).map {
      x => if (x.isEmpty) loginInfoDAO.insert(loginInfo, userId)
    }
  }
}
