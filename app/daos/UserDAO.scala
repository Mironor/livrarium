package daos

import com.mohiva.play.silhouette.api.LoginInfo
import daos.DBTableDefinitions.{DBUser, LoginInfos, Users}
import play.api.Play.current
import play.api.db.slick._

import scala.concurrent.Future
import scala.slick.driver.PostgresDriver.simple._


class UserDAO {

  private val slickUsers = TableQuery[Users]
  private val slickLoginInfos = TableQuery[LoginInfos]

  /**
   * Finds a user by its id
   * @param userId user's id
   * @return The found user (None if user could not be found)
   */
  def findById(userId: Long): Future[Option[DBUser]] =
    Future.successful {
      DB withSession { implicit session =>
        slickUsers.filter(_.id === userId).firstOption
      }
    }

  /**
   * Finds a user by its login info.
   * @param loginInfo The login info of the user to find.
   * @return The found user or None if no user for the given login info could be found.
   */
  def findByLoginInfo(loginInfo: LoginInfo): Future[Option[DBUser]] =
    Future.successful {
      DB withSession { implicit session =>
        (for {
          lInfo <- slickLoginInfos if lInfo.providerID === loginInfo.providerID && lInfo.providerKey === loginInfo.providerKey
          user <- slickUsers if user.id === lInfo.idUser
        } yield user).firstOption
      }
    }

  /**
   * Saves a user.
   *
   * @param user The user to save.
   * @return The saved user.
   */
  def insert(user: DBUser): Future[DBUser] =
    Future.successful {
      DB withSession { implicit session =>
        val userId = (slickUsers returning slickUsers.map(_.id)) += user
        user.copy(id = Option(userId))
      }
    }

  /**
   * Saves a user.
   *
   * @param user The user to save.
   * @return The saved user.
   */
  def update(user: DBUser): Future[DBUser] =
    Future.successful {
      DB withSession { implicit session =>
        slickUsers.filter(_.id === user.id).update(user)
        user
      }
    }

}
