package daos

import com.mohiva.play.silhouette.api.LoginInfo
import daos.silhouette.LoginInfoDAO
import fixtures.UserFixture
import helpers.LivrariumSpecification
import org.specs2.matcher.ThrownMessages
import org.specs2.specification.AroundExample

class LoginInfoDAOSpec extends LivrariumSpecification with AroundExample with ThrownMessages {

  protected def bootstrapFixtures(): Unit = {
    await(UserFixture.initFixture())
  }

  "LoginInfo DAO" should {
    "find login info if it exists" in {
      // Given
      val loginInfoDAO = new LoginInfoDAO

      // When
      val foundLoginInfo = await(loginInfoDAO.find(UserFixture.testUserLoginInfo))

      // Then
      foundLoginInfo must beSome
    }

    "find None if login info does not exist" in {
      // Given
      val loginInfoDAO = new LoginInfoDAO

      val loginInfo = LoginInfo("some id", "some key")

      // When
      val foundLoginInfo = await(loginInfoDAO.find(loginInfo))

      // Then
      foundLoginInfo must beNone
    }

    "insert new LoginInfo" in {
      // Given
      val loginInfoDAO = new LoginInfoDAO

      val newLoginInfo = LoginInfo("some id", "some key")

      // When
      await(loginInfoDAO.insert(newLoginInfo, UserFixture.testUserId))
      val loginInfo = await(loginInfoDAO.find(newLoginInfo))

      // Then
      loginInfo must beSome
    }
  }
}