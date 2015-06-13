package daos.silhouette

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.PasswordInfo
import com.mohiva.play.silhouette.impl.daos.DelegableAuthInfoDAO
import daos.DBTableDefinitions.{DBPasswordInfo, LoginInfos, PasswordInfos}
import play.api.Play
import play.api.db.slick._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import slick.driver.JdbcProfile
import slick.driver.PostgresDriver.api._
import slick.lifted.TableQuery

import scala.concurrent.Future


class PasswordInfoDAO extends DelegableAuthInfoDAO[PasswordInfo] with HasDatabaseConfig[JdbcProfile] {
  val dbConfig = DatabaseConfigProvider.get[JdbcProfile](Play.current)


  private val slickPasswordInfos = TableQuery[PasswordInfos]
  private val slickLoginInfos = TableQuery[LoginInfos]

  /**
   * Finds the password info which is linked with the specified login info.
   *
   * @param loginInfo The linked login info.
   * @return The retrieved password info or None if no password info could be retrieved for the given login info.
   */
  def find(loginInfo: LoginInfo): Future[Option[PasswordInfo]] = db.run {
    (for {
      dbLoginInfo <- slickLoginInfos if dbLoginInfo.providerID === loginInfo.providerID && dbLoginInfo.providerKey === loginInfo.providerKey
      dbPasswordInfo <- slickPasswordInfos if dbPasswordInfo.idLoginInfo === dbLoginInfo.id
    } yield dbPasswordInfo).result.headOption
  }.map {
    _.flatMap(dbPasswordInfo => Some(PasswordInfo(dbPasswordInfo.hasher, dbPasswordInfo.password, dbPasswordInfo.salt)))
  }

  /**
   * Saves the password info.
   *
   * @param loginInfo The login info for which the auth info should be saved.
   * @param authInfo The password info to save.
   * @return The saved password info or None if the password info couldn't be saved.
   */
  def save(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] = db.run {
    for {
      loginInfoOption <- slickLoginInfos.filter(x => x.providerID === loginInfo.providerID && x.providerKey === loginInfo.providerKey)
        .result.headOption

      loginInfoIdOption = loginInfoOption.flatMap(_.id)

      loginInfoId = loginInfoIdOption.getOrElse(throw new SilhouetteDAOException("Associated LoginInfo not found"))

    } yield loginInfoId
  }.flatMap {
    loginInfoId => db.run(slickPasswordInfos += DBPasswordInfo(loginInfoId, authInfo.hasher, authInfo.password, authInfo.salt))
  }.map(_ => authInfo)

  override def add(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] = getLoginInfoIdQuery(loginInfo).map {
    loginInfoId => slickPasswordInfos += DBPasswordInfo(loginInfoId, authInfo.hasher, authInfo.password, authInfo.salt)
  }.map(_ => authInfo)

  private def getLoginInfoIdQuery(loginInfo: LoginInfo) = db.run {
    slickLoginInfos.filter(x => x.providerID === loginInfo.providerID && x.providerKey === loginInfo.providerKey).result.headOption
  }.map {
    _.flatMap(_.id).getOrElse(throw new SilhouetteDAOException("Associated LoginInfo not found"))
  }

  override def update(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] = getLoginInfoIdQuery(loginInfo).map {
    loginInfoId => slickPasswordInfos.update(DBPasswordInfo(loginInfoId, authInfo.hasher, authInfo.password, authInfo.salt))
  }.map(_ => authInfo)

  override def remove(loginInfo: LoginInfo): Future[Unit] = getLoginInfoIdQuery(loginInfo).map {
    loginInfoId => slickPasswordInfos.filter(_.idLoginInfo === loginInfoId).delete
  }
}
