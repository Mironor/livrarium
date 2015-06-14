package daos.silhouette

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.daos.DelegableAuthInfoDAO
import com.mohiva.play.silhouette.impl.providers.OAuth2Info
import daos.DBTableDefinitions.{DBOAuth2Info, LoginInfos, OAuth2Infos}
import play.api.db.slick._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scaldi.{Injectable, Injector}
import slick.driver.JdbcProfile
import slick.driver.PostgresDriver.api._
import slick.lifted.TableQuery

import scala.concurrent.Future


class OAuth2InfoDAO(implicit inj: Injector) extends DelegableAuthInfoDAO[OAuth2Info] with HasDatabaseConfig[JdbcProfile] with Injectable {
  val dbConfig = inject[DatabaseConfigProvider].get[JdbcProfile]


  private val slickOAuth2Infos = TableQuery[OAuth2Infos]
  private val slickLoginInfos = TableQuery[LoginInfos]

  /**
   * Finds the OAuth2 info which is linked with the specified login info.
   *
   * @param loginInfo The linked login info.
   * @return The retrieved OAuth2 info or None if no OAuth2 info could be retrieved for the given login info.
   */
  def find(loginInfo: LoginInfo): Future[Option[OAuth2Info]] = db.run {
    (for {
      dbLoginInfo <- slickLoginInfos if dbLoginInfo.providerID === loginInfo.providerID && dbLoginInfo.providerKey === loginInfo.providerKey
      dbOAuth2Info <- slickOAuth2Infos if dbOAuth2Info.idLoginInfo === dbLoginInfo.id
    } yield dbOAuth2Info).result.headOption
  }.map {
    _.map {
      dbOAuth1Info => OAuth2Info(dbOAuth1Info.accessToken, dbOAuth1Info.tokenType, dbOAuth1Info.expiresIn, dbOAuth1Info.refreshToken)
    }
  }

  /**
   * Saves the OAuth2 info.
   *
   * @param loginInfo The login info for which the auth info should be saved.
   * @param authInfo The OAuth2 info to save.
   * @return The saved OAuth2 info or None if the OAuth2 info couldn't be saved.
   */
  def save(loginInfo: LoginInfo, authInfo: OAuth2Info): Future[OAuth2Info] = db.run {
    for {
      foundLoginInfo <- slickLoginInfos.filter(x => x.providerID === loginInfo.providerID && x.providerKey === loginInfo.providerKey)
        .result.headOption
      loginInfoIdOption = foundLoginInfo.flatMap(_.id)
      loginInfoId = loginInfoIdOption.getOrElse(throw new SilhouetteDAOException("Associated LoginInfo not found"))
      dbOAuth2Info <- slickOAuth2Infos.filter(_.idLoginInfo === loginInfoId).result.headOption
    } yield (loginInfoId, dbOAuth2Info)
  }.flatMap {
    case (loginInfoId, Some(a2Info)) => db.run(slickOAuth2Infos.update(DBOAuth2Info(loginInfoId, authInfo.accessToken, authInfo.tokenType, authInfo.expiresIn, authInfo.refreshToken)))
    case (loginInfoId, None) => db.run(slickOAuth2Infos += DBOAuth2Info(loginInfoId, authInfo.accessToken, authInfo.tokenType, authInfo.expiresIn, authInfo.refreshToken))
  }.map(_ => authInfo)

  override def add(loginInfo: LoginInfo, authInfo: OAuth2Info): Future[OAuth2Info] = getLoginInfoIdQuery(loginInfo).map {
    loginInfoId => slickOAuth2Infos += DBOAuth2Info(loginInfoId, authInfo.accessToken, authInfo.tokenType, authInfo.expiresIn, authInfo.refreshToken)
  }.map(_ => authInfo)

  private def getLoginInfoIdQuery(loginInfo: LoginInfo) = db.run {
    slickLoginInfos.filter(x => x.providerID === loginInfo.providerID && x.providerKey === loginInfo.providerKey).result.headOption
  }.map {
    _.flatMap(_.id).getOrElse(throw new SilhouetteDAOException("Associated LoginInfo not found"))
  }

  override def update(loginInfo: LoginInfo, authInfo: OAuth2Info): Future[OAuth2Info] = getLoginInfoIdQuery(loginInfo).map {
    loginInfoId => slickOAuth2Infos.update(DBOAuth2Info(loginInfoId, authInfo.accessToken, authInfo.tokenType, authInfo.expiresIn, authInfo.refreshToken))
  }.map(_ => authInfo)

  override def remove(loginInfo: LoginInfo): Future[Unit] = getLoginInfoIdQuery(loginInfo).map {
    loginInfoId => slickOAuth2Infos.filter(_.idLoginInfo === loginInfoId).delete
  }
}


