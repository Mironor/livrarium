package models.silhouette

import com.mohiva.play.silhouette.contrib.daos.DelegableAuthInfoDAO
import com.mohiva.play.silhouette.core.LoginInfo
import com.mohiva.play.silhouette.core.providers.OAuth2Info
import models.DBTableDefinitions.{DBOAuth2Info, LoginInfos, OAuth2Infos}
import play.api.Play.current
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick._

import scala.concurrent.Future


class OAuth2InfoDAO extends DelegableAuthInfoDAO[OAuth2Info] {

  private val slickOAuth2Infos = TableQuery[OAuth2Infos]
  private val slickLoginInfos = TableQuery[LoginInfos]

  /**
   * Finds the OAuth2 info which is linked with the specified login info.
   *
   * @param loginInfo The linked login info.
   * @return The retrieved OAuth2 info or None if no OAuth2 info could be retrieved for the given login info.
   */
  def find(loginInfo: LoginInfo): Future[Option[OAuth2Info]] = {
    Future.successful(
      DB withSession { implicit session =>

        val dbOAuth2InfoOption = (for {
          dbLoginInfo <- slickLoginInfos if dbLoginInfo.providerID === loginInfo.providerID && dbLoginInfo.providerKey === loginInfo.providerKey
          dbOAuth2Info <- slickOAuth2Infos if dbOAuth2Info.idLoginInfo === dbLoginInfo.id
        } yield dbOAuth2Info).firstOption

        dbOAuth2InfoOption.flatMap(dbOAuth1Info => Some(OAuth2Info(dbOAuth1Info.accessToken, dbOAuth1Info.tokenType, dbOAuth1Info.expiresIn, dbOAuth1Info.refreshToken)))
      }
    )
  }

  /**
   * Saves the OAuth2 info.
   *
   * @param loginInfo The login info for which the auth info should be saved.
   * @param authInfo The OAuth2 info to save.
   * @return The saved OAuth2 info or None if the OAuth2 info couldn't be saved.
   */
  def save(loginInfo: LoginInfo, authInfo: OAuth2Info): Future[OAuth2Info] = {
    Future.successful(
      DB withSession { implicit session =>

        val loginInfoId = slickLoginInfos
          .filter(x => x.providerID === loginInfo.providerID && x.providerKey === loginInfo.providerKey)
          .first.id.get

        val dbOAuth2Info = slickOAuth2Infos.filter(_.idLoginInfo === loginInfoId).firstOption

        dbOAuth2Info match {
          case Some(a2Info) => slickOAuth2Infos update DBOAuth2Info(loginInfoId, authInfo.accessToken, authInfo.tokenType, authInfo.expiresIn, authInfo.refreshToken)
          case None => slickOAuth2Infos insert DBOAuth2Info(loginInfoId, authInfo.accessToken, authInfo.tokenType, authInfo.expiresIn, authInfo.refreshToken)
        }
        authInfo
      }
    )
  }
}

