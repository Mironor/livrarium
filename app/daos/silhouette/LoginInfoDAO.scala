package daos.silhouette

import com.mohiva.play.silhouette.api.LoginInfo
import daos.DBTableDefinitions.{DBLoginInfo, LoginInfos}
import play.api.Play.current
import play.api.db.slick._

import scala.concurrent.Future
import scala.slick.lifted.TableQuery
import scala.slick.driver.PostgresDriver.simple._

class LoginInfoDAO {

  private val slickLoginInfos = TableQuery[LoginInfos]

  /**
   * Find supplied login info in the database
   * @return a promise of found login info (None if not found)
   */
  def find(loginInfo: LoginInfo): Future[Option[DBLoginInfo]] =
    Future.successful {
      DB withSession { implicit session =>
        slickLoginInfos.filter(x => x.providerID === loginInfo.providerID && x.providerKey === loginInfo.providerKey)
          .firstOption
      }
    }

  /**
   * Inserts a new LoginInfo in the database
   * @param loginInfo new login info
   * @param userId the user to whom the login info will be attached
   * @return The saved user.
   */
  def insert(loginInfo: LoginInfo, userId: Long): Future[DBLoginInfo] =
    Future.successful {
      DB withSession { implicit session =>
        val loginInfoId = slickLoginInfos returning slickLoginInfos.map(_.id) += DBLoginInfo(None, userId, loginInfo.providerID, loginInfo.providerKey)
        DBLoginInfo(Option(loginInfoId), userId, loginInfo.providerID, loginInfo.providerKey)
      }
    }
}
