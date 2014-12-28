package fixtures

import com.mohiva.play.silhouette.api.LoginInfo
import daos.DBTableDefinitions
import DBTableDefinitions.{LoginInfos, DBLoginInfo, Users}
import models.User
import play.api.Play.current
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick._

import scala.concurrent.Future
import scala.slick.lifted.TableQuery

object UserFixture {

  val slickUsers = TableQuery[Users]
  val slickLoginInfos = TableQuery[LoginInfos]

  val testUserId = 1
  val testUserLoginInfo = LoginInfo("key", "value")
  val testUser = User(Option(testUserId), testUserLoginInfo, Option("test@test.test"), Option("someUrl"))

  val otherUserId = 2
  val otherUserLoginInfo = LoginInfo("key_other", "value_other")
  val otherUser = User(Option(otherUserId), otherUserLoginInfo, None, None)

  def initFixture(): Future[_] = {
    Future.successful {
      DB withSession { implicit session =>
        slickUsers ++= Seq(testUser.toDBUser, otherUser.toDBUser)
        slickLoginInfos ++= Seq(
          DBLoginInfo(None, testUserId, testUserLoginInfo.providerID, testUserLoginInfo.providerKey),
          DBLoginInfo(None, otherUserId, otherUserLoginInfo.providerID, otherUserLoginInfo.providerKey)
        )
      }
    }
  }

}
