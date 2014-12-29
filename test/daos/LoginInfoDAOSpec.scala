package daos

import com.mohiva.play.silhouette.api.LoginInfo
import daos.silhouette.LoginInfoDAO
import fixtures.UserFixture
import globals.TestGlobal
import helpers.LivrariumSpecification
import org.specs2.execute.AsResult
import org.specs2.matcher.ThrownMessages
import org.specs2.specification.AroundExample
import play.api.test.FakeApplication

class LoginInfoDAOSpec extends LivrariumSpecification with AroundExample with ThrownMessages {

  /**
   * This automatically handles up and down evolutions at the beginning and at the end of a spec respectively
   */
  def around[T: AsResult](t: => T) = {
    val app = FakeApplication(withGlobal = Some(TestGlobal), additionalConfiguration = inMemoryDatabase())
    running(app) {
      await(UserFixture.initFixture())

      AsResult(t)
    }
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
