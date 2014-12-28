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
   * Method created only for testing purposes
   * @return a promise of all of the login infos
   */
  def findAll(): Future[List[DBLoginInfo]] =
    Future.successful {
      DB withSession { implicit session =>
        slickLoginInfos.list
      }
    }

  /**
   * Inserts a new LoginInfo in the database
   * @param loginInfo new login info
   * @param userId the user to whom the login info will be attached
   * @return The saved user.
   */
  def insertLoginInfo(loginInfo: LoginInfo, userId: Long): Future[DBLoginInfo] =
    Future.successful {
      DB withSession { implicit session =>
        val loginInfoId = slickLoginInfos returning slickLoginInfos.map(_.id) += DBLoginInfo(None, userId, loginInfo.providerID, loginInfo.providerKey)
        DBLoginInfo(Option(loginInfoId), userId, loginInfo.providerID, loginInfo.providerKey)
      }
    }
}
