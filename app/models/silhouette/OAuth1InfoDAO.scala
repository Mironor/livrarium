package models.silhouette

import com.mohiva.play.silhouette.contrib.daos.DelegableAuthInfoDAO
import com.mohiva.play.silhouette.core.LoginInfo
import com.mohiva.play.silhouette.core.providers.OAuth1Info
import models.DBTableDefinitions.{DBOAuth1Info, LoginInfos, OAuth1Infos}
import play.api.Play.current
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick._

import scala.concurrent.Future


class OAuth1InfoDAO extends DelegableAuthInfoDAO[OAuth1Info] {

  private val slickOAuth1Infos = TableQuery[OAuth1Infos]
  private val slickLoginInfos = TableQuery[LoginInfos]

  /**
   * Finds the OAuth1 info which is linked with the specified login info.
   *
   * @param loginInfo The linked login info.
   * @return The retrieved OAuth1 info or None if no OAuth1 info could be retrieved for the given login info.
   */
  def find(loginInfo: LoginInfo): Future[Option[OAuth1Info]] = {
    Future.successful(
      DB withSession { implicit session =>

        val dbOAuth1InfoOption = (for {
          dbLoginInfo <- slickLoginInfos if dbLoginInfo.providerID === loginInfo.providerID && dbLoginInfo.providerKey === loginInfo.providerKey
          dbOAuth1Info <- slickOAuth1Infos if dbOAuth1Info.idLoginInfo === dbLoginInfo.id
        } yield dbOAuth1Info).firstOption

        dbOAuth1InfoOption.flatMap(dbOAuth1Info => Some(OAuth1Info(dbOAuth1Info.token, dbOAuth1Info.secret)))
      }
    )
  }

  /**
   * Saves the OAuth1 info.
   *
   * @param loginInfo The login info for which the auth info should be saved.
   * @param authInfo The OAuth1 info to save.
   * @return The saved OAuth1 info or None if the OAuth1 info couldn't be saved.
   */
  def save(loginInfo: LoginInfo, authInfo: OAuth1Info): Future[OAuth1Info] = {
    Future.successful(
      DB withSession { implicit session =>

        val loginInfoId = slickLoginInfos
          .filter(x => x.providerID === loginInfo.providerID && x.providerKey === loginInfo.providerKey)
          .first.id.get

        val dbOAuth1Info = slickOAuth1Infos.filter(_.idLoginInfo === loginInfoId).firstOption

        dbOAuth1Info match {
          case Some(a1Info) => slickOAuth1Infos update DBOAuth1Info(loginInfoId, authInfo.token, authInfo.secret)
          case None => slickOAuth1Infos insert DBOAuth1Info(loginInfoId, authInfo.token, authInfo.secret)
        }
        authInfo
      }
    )
  }
}

