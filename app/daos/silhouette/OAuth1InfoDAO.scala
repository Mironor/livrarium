package daos.silhouette

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.daos.DelegableAuthInfoDAO
import com.mohiva.play.silhouette.impl.providers.OAuth1Info
import daos.DBTableDefinitions.{DBOAuth1Info, LoginInfos, OAuth1Infos}
import scaldi.{Injectable, Injector}
import slick.driver.JdbcProfile
import play.api.db.slick._
import slick.driver.PostgresDriver.api._
import slick.lifted.TableQuery

import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future


class OAuth1InfoDAO(implicit inj: Injector) extends DelegableAuthInfoDAO[OAuth1Info] with HasDatabaseConfig[JdbcProfile] with Injectable {
  val dbConfig = inject[DatabaseConfigProvider].get[JdbcProfile]


  private val slickOAuth1Infos = TableQuery[OAuth1Infos]
  private val slickLoginInfos = TableQuery[LoginInfos]

  /**
   * Finds the OAuth1 info which is linked with the specified login info.
   *
   * @param loginInfo The linked login info.
   * @return The retrieved OAuth1 info or None if no OAuth1 info could be retrieved for the given login info.
   */
  def find(loginInfo: LoginInfo): Future[Option[OAuth1Info]] = db.run {
    (for {
      dbLoginInfo <- slickLoginInfos if dbLoginInfo.providerID === loginInfo.providerID && dbLoginInfo.providerKey === loginInfo.providerKey
      dbOAuth1Info <- slickOAuth1Infos if dbOAuth1Info.idLoginInfo === dbLoginInfo.id
    } yield dbOAuth1Info).result.headOption
  }.map(_.map(dbOAuth1Info => OAuth1Info(dbOAuth1Info.token, dbOAuth1Info.secret)))


  /**
   * Saves the OAuth1 info.
   *
   * @param loginInfo The login info for which the auth info should be saved.
   * @param authInfo The OAuth1 info to save.
   * @return The saved OAuth1 info or None if the OAuth1 info couldn't be saved.
   */
  def save(loginInfo: LoginInfo, authInfo: OAuth1Info): Future[OAuth1Info] = db.run {
    for {
      foundLoginInfo <- slickLoginInfos.filter(x => x.providerID === loginInfo.providerID && x.providerKey === loginInfo.providerKey)
        .result.headOption
      loginInfoIdOption = foundLoginInfo.flatMap(_.id)
      loginInfoId = loginInfoIdOption.getOrElse(throw new SilhouetteDAOException("Associated LoginInfo not found"))
      dbOAuth1Info <- slickOAuth1Infos.filter(_.idLoginInfo === loginInfoId).result.headOption
    } yield (loginInfoId, dbOAuth1Info)
  }.flatMap {
    case (loginInfoId, Some(a1Info)) => db.run(slickOAuth1Infos.update(DBOAuth1Info(loginInfoId, authInfo.token, authInfo.secret)))
    case (loginInfoId, None) => db.run(slickOAuth1Infos += DBOAuth1Info(loginInfoId, authInfo.token, authInfo.secret))
  }.map(_ => authInfo)

  override def add(loginInfo: LoginInfo, authInfo: OAuth1Info): Future[OAuth1Info] = getLoginInfoIdQuery(loginInfo).map {
    loginInfoId => slickOAuth1Infos += DBOAuth1Info(loginInfoId, authInfo.token, authInfo.secret)
  }.map(_ => authInfo)

  private def getLoginInfoIdQuery(loginInfo: LoginInfo) = db.run {
    slickLoginInfos.filter(x => x.providerID === loginInfo.providerID && x.providerKey === loginInfo.providerKey).result.headOption
  }.map {
    _.flatMap(_.id).getOrElse(throw new SilhouetteDAOException("Associated LoginInfo not found"))
  }

  override def update(loginInfo: LoginInfo, authInfo: OAuth1Info): Future[OAuth1Info] = getLoginInfoIdQuery(loginInfo).map {
    loginInfoId => slickOAuth1Infos.update(DBOAuth1Info(loginInfoId, authInfo.token, authInfo.secret))
  }.map(_ => authInfo)

  override def remove(loginInfo: LoginInfo): Future[Unit] = getLoginInfoIdQuery(loginInfo).map {
    loginInfoId => slickOAuth1Infos.filter(_.idLoginInfo === loginInfoId).delete
  }

}

