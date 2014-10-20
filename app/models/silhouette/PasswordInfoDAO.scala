package models.silhouette

import com.mohiva.play.silhouette.contrib.daos.DelegableAuthInfoDAO
import com.mohiva.play.silhouette.core.LoginInfo
import com.mohiva.play.silhouette.core.providers.PasswordInfo
import models.DBTableDefinitions.{DBPasswordInfo, LoginInfos, PasswordInfos}
import play.api.Play.current
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick._

import scala.concurrent.Future


class PasswordInfoDAO extends DelegableAuthInfoDAO[PasswordInfo] {

  private val slickPasswordInfos = TableQuery[PasswordInfos]
  private val slickLoginInfos = TableQuery[LoginInfos]

  /**
   * Finds the password info which is linked with the specified login info.
   *
   * @param loginInfo The linked login info.
   * @return The retrieved password info or None if no password info could be retrieved for the given login info.
   */
  def find(loginInfo: LoginInfo): Future[Option[PasswordInfo]] = {
    Future.successful {
      DB withSession { implicit session =>

        val dbPasswordInfoOption = (for {
          dbLoginInfo <- slickLoginInfos if dbLoginInfo.providerID === loginInfo.providerID && dbLoginInfo.providerKey === loginInfo.providerKey
          dbPasswordInfo <- slickPasswordInfos if dbPasswordInfo.idLoginInfo === dbLoginInfo.id
        } yield dbPasswordInfo).firstOption

        dbPasswordInfoOption.flatMap(dbPasswordInfo => Some(PasswordInfo(dbPasswordInfo.hasher, dbPasswordInfo.password, dbPasswordInfo.salt)))
      }
    }
  }

  /**
   * Saves the password info.
   *
   * @param loginInfo The login info for which the auth info should be saved.
   * @param authInfo The password info to save.
   * @return The saved password info or None if the password info couldn't be saved.
   */
  def save(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] = {
    Future.successful {
      DB withSession { implicit session =>

        val loginInfoId = slickLoginInfos
          .filter(x => x.providerID === loginInfo.providerID && x.providerKey === loginInfo.providerKey)
          .first.id

        slickPasswordInfos insert DBPasswordInfo(loginInfoId.get, authInfo.hasher, authInfo.password, authInfo.salt)
        authInfo
      }
    }
  }
}

