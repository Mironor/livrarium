package fixtures

import com.mohiva.play.silhouette.api.LoginInfo
import models.DBTableDefinitions.{LoginInfos, DBLoginInfo, Users}
import play.api.Play.current
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick._
import services.User

import scala.concurrent.Future
import scala.slick.lifted.TableQuery

object UserFixture {

  val slickUsers = TableQuery[Users]
  val slickLoginInfos = TableQuery[LoginInfos]

  val testUserId = 1
  val testUserLoginInfo = LoginInfo("key", "value")
  val testUser = User(Option(testUserId), testUserLoginInfo, None, None)

  def initFixture(): Future[_] = {
    Future.successful {
      DB withSession { implicit session =>
        slickUsers += testUser.toDBUser
        slickLoginInfos += DBLoginInfo(None, testUserId, testUserLoginInfo.providerID, testUserLoginInfo.providerKey)
      }
    }
  }

}
