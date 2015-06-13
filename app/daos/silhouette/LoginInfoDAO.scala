package daos.silhouette

import com.mohiva.play.silhouette.api.LoginInfo
import daos.DBTableDefinitions.{DBLoginInfo, LoginInfos}
import play.api.Play
import play.api.db.slick._
import slick.driver.PostgresDriver.api._

import play.api.libs.concurrent.Execution.Implicits.defaultContext

import slick.driver.JdbcProfile
import scala.concurrent.Future
import slick.lifted.TableQuery

class LoginInfoDAO extends HasDatabaseConfig[JdbcProfile] {
  val dbConfig = DatabaseConfigProvider.get[JdbcProfile](Play.current)

  private val slickLoginInfos = TableQuery[LoginInfos]

  /**
   * Find supplied login info in the database
   * @return a promise of found login info (None if not found)
   */
  def find(loginInfo: LoginInfo): Future[Option[DBLoginInfo]] = db.run {
    slickLoginInfos.filter(x => x.providerID === loginInfo.providerID && x.providerKey === loginInfo.providerKey)
      .result.headOption
  }

  /**
   * Returns all login infos stored in the database
   * @return a promise of a list of all login infos in the database
   */
  def findAll(): Future[Seq[DBLoginInfo]] = db.run {
    slickLoginInfos.result
  }

  /**
   * Inserts a new LoginInfo in the database
   * @param loginInfo new login info
   * @param userId the user to whom the login info will be attached
   * @return The saved user.
   */
  def insert(loginInfo: LoginInfo, userId: Long): Future[DBLoginInfo] = db.run {
    slickLoginInfos.returning(slickLoginInfos.map(_.id)) += DBLoginInfo(None, userId, loginInfo.providerID, loginInfo.providerKey)
  }.map(id => DBLoginInfo(Option(id), userId, loginInfo.providerID, loginInfo.providerKey))
}
