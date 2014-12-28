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
    "insert new LoginInfo" in {
      // Given
      val loginInfoDAO = new LoginInfoDAO

      val newLoginInfo = LoginInfo("some id", "some key")

      val targetLoginInfosCountAfterInsert = await(loginInfoDAO.findAll()).length + 1

      // When
      await(loginInfoDAO.insertLoginInfo(newLoginInfo, UserFixture.testUserId))
      val loginInfosCountAfterInsert = await(loginInfoDAO.findAll()).length

      // Then
      loginInfosCountAfterInsert must beEqualTo(targetLoginInfosCountAfterInsert)
    }
  }
}
