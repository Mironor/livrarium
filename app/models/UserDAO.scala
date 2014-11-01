package models

import com.mohiva.play.silhouette.core.LoginInfo
import scala.slick.jdbc.JdbcBackend.Session
import models.DBTableDefinitions.Users
import models.DBTableDefinitions._
import scala.slick.driver.PostgresDriver.simple._
import play.api.db.slick._
import play.api.Play.current

import scala.concurrent.Future


class UserDAO {

  private val slickUsers = TableQuery[Users]
  private val slickLoginInfos = TableQuery[LoginInfos]

  /**
   * Finds a user by its login info.
   *
   * @param loginInfo The login info of the user to find.
   * @return The found user or None if no user for the given login info could be found.
   */
  def find(loginInfo: LoginInfo): Future[Option[DBUser]] = {
    Future.successful {
      DB withSession { implicit session =>
        (for {
          lInfo <- slickLoginInfos if lInfo.providerID === loginInfo.providerID && lInfo.providerKey === loginInfo.providerKey
          user <- slickUsers if user.id === lInfo.idUser
        } yield user).firstOption
      }
    }
  }

  /**
   * Saves a user.
   *
   * @param user The user to save.
   * @return The saved user.
   */
  def save(user: DBUser): Future[DBUser] = {
    Future.successful {
      DB withSession { implicit session =>
        val returnedUserId: Long = saveDbUser(user)
        user.copy(id = Option(returnedUserId))
      }
    }
  }

  /**
   * Saves a user with login info.
   * @param user The user to save.
   * @param loginInfo a user may have LoginInfo attached
   * @return The saved user.
   */
  def saveWithLoginInfo(user: DBUser, loginInfo: LoginInfo): Future[DBUser] = {
    Future.successful {
      DB withSession { implicit session =>
        val returnedUserId: Long = saveDbUser(user)

        saveLoginInfo(loginInfo, returnedUserId)

        user.copy(id = Option(returnedUserId))
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

  private def createDbUser(dbUser: DBUser)(implicit session: Session): Long = (slickUsers returning slickUsers.map(_.id)) += dbUser

  private def saveLoginInfo(loginInfo: LoginInfo, userId: Long)(implicit session: Session): Unit = {
    // Insert if it does not exist yet
    val dbLoginInfoOption = slickLoginInfos.filter(info => info.providerID === loginInfo.providerID && info.providerKey === loginInfo.providerKey)
      .firstOption
    if (dbLoginInfoOption.isEmpty) slickLoginInfos.insert(DBLoginInfo(None, userId, loginInfo.providerID, loginInfo.providerKey))
  }
}
