package services

import com.mohiva.play.silhouette.api.LoginInfo
import daos.silhouette.LoginInfoDAO
import fixtures.UserFixture
import globals.TestGlobal
import helpers.LivrariumSpecification
import models.User
import org.specs2.execute.AsResult
import org.specs2.matcher.ThrownMessages
import org.specs2.specification.AroundExample
import play.api.test.FakeApplication

class UserServiceSpec extends LivrariumSpecification with AroundExample with ThrownMessages {

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

  "User service" should {
    "retrieve user by login info" in {
      // Given
      val userService = new UserService

      // When
      val user = await(userService.retrieve(UserFixture.testUserLoginInfo)).getOrElse(fail("User was not found"))

      // Then
      user.email must beEqualTo(UserFixture.testUser.email)
      user.avatarURL must beEqualTo(UserFixture.testUser.avatarURL)
    }

    "save a new user" in {
      // Given
      val userService = new UserService
      val newEmail = Option("new email")
      val newAvatarUrl = Option("new avatar url")
      val newLoginInfo = LoginInfo("new key", "new value")
      val newUser = User(None, newLoginInfo, newEmail, newAvatarUrl)

      // When
      await(userService.save(newUser))
      val user = await(userService.retrieve(newLoginInfo)).getOrElse(fail("User was not found"))

      // Then
      user.email must beEqualTo(newEmail)
      user.avatarURL must beEqualTo(newAvatarUrl)
    }

    "create new login info attached to the user if the login info does not exist" in {
      // Given
      val userService = new UserService
      val loginInfoDAO = inject[LoginInfoDAO]
      val newLoginInfo = LoginInfo("new key", "new value")
      val newUser = User(None, newLoginInfo, None, None)

      // When
      await(userService.save(newUser))
      val loginInfo = await(loginInfoDAO.find(newLoginInfo))

      // Then
      loginInfo must beSome
    }

    "save an already created user" in {
      // Given
      val userService = new UserService

      val updatedEmail = Option("updated email")
      val updatedAvatarUrl = Option("updated avatar url")

      // When
      await(userService.save(UserFixture.testUser.copy(email = updatedEmail, avatarURL = updatedAvatarUrl)))
      val user = await(userService.retrieve(UserFixture.testUserLoginInfo)).getOrElse(fail("User was not found"))

      // Then
      user.email must beEqualTo(updatedEmail)
      user.avatarURL must beEqualTo(updatedAvatarUrl)

    }

  }
}
