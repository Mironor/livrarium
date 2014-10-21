package models

import com.mohiva.play.silhouette.core.{LoginInfo, Identity}
import scala.slick.jdbc.JdbcBackend.Session
import models.DBTableDefinitions.Users
import models.DBTableDefinitions._
import scala.slick.driver.PostgresDriver.simple._
import play.api.db.slick._
import play.api.Play.current

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


class UserDAO {

  private val slickUsers = TableQuery[Users]
  private val slickLoginInfos = TableQuery[LoginInfos]

  /**
   * Finds a user by its login info.
   *
   * @param loginInfo The login info of the user to find.
   * @return The found user or None if no user for the given login info could be found.
   */
  def find(loginInfo: LoginInfo) = {
    Future.successful {
      DB withSession { implicit session =>
        val dbUser = (for {
          loginInfo <- slickLoginInfos if loginInfo.providerID === loginInfo.providerID && loginInfo.providerKey === loginInfo.providerKey
          user <- slickUsers if user.id === loginInfo.idUser
        } yield user).firstOption

        dbUser match {
          case Some(user) => Some(User(user.id, loginInfo, user.email, user.avatarURL))
          case None => None
        }
      }
    }
  }

  /**
   * Finds a user by its user ID.
   *
   * @param userId The ID of the user to find.
   * @return The found user or None if no user for the given ID could be found.
   */
  def find(userId: Long) = {
    Future.successful {
      DB withSession { implicit session =>
        val retrievedUser = slickUsers.filter(_.id === userId).firstOption

        retrievedUser match {
          case Some(dbUser) =>
            // At least one LoginInfo should exist for this user
            val dbLoginInfo = slickLoginInfos.filter(_.idUser === userId).first

            Some(User(dbUser.id, LoginInfo(dbLoginInfo.providerID,
              dbLoginInfo.providerKey), dbUser.email, dbUser.avatarURL))

          case None => None
        }
      }
    }
  }

  def create(user: DBUser): Future[Long] = {
    Future.successful {
      DB withSession { implicit session =>
        createDbUser(user)
      }
    }
  }

  private def createDbUser(dbUser: DBUser)(implicit session: Session): Long = (slickUsers returning slickUsers.map(_.id)) += dbUser

  /**
   * Saves a user.
   *
   * @param user The user to save.
   * @return The saved user.
   */
  def save(user: User) = {
    Future.successful {
      DB withSession { implicit session =>
        val dbUser = DBUser(user.id, user.email, user.avatarURL)
        val returnedUserId: Long = saveDbUser(dbUser)

        // Insert if it does not exist yet
        val dbLoginInfoOption = slickLoginInfos.filter(info => info.providerID === user.loginInfo.providerID && info.providerKey === user.loginInfo.providerKey).firstOption
        if (dbLoginInfoOption.isEmpty) slickLoginInfos.insert(DBLoginInfo(None, returnedUserId, user.loginInfo.providerID, user.loginInfo.providerKey))

        user // We do not change the user => return it
      }
    }
  }

  private def saveDbUser(dbUser: DBUser)(implicit session: Session): Long = {
    slickUsers.filter(_.id === dbUser.id).firstOption match {
      case Some(userFound) =>
        slickUsers.filter(_.id === dbUser.id).update(dbUser)
        dbUser.id.get
      case None => createDbUser(dbUser)
    }
  }

}
