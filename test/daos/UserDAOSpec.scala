package daos

import daos.DBTableDefinitions.DBUser
import fixtures.UserFixture
import globals.TestGlobal
import helpers.LivrariumSpecification
import org.specs2.execute.AsResult
import org.specs2.matcher.ThrownMessages
import org.specs2.specification.AroundExample
import play.api.test.FakeApplication

class UserDAOSpec extends LivrariumSpecification with AroundExample with ThrownMessages {

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

  "User DAO" should {
    "find user by its id" in {
      // Given
      val userDAO = new UserDAO

      // When
      val user = await(userDAO.findById(UserFixture.testUserId)).getOrElse(fail("User cannot be found"))

      // Then
      user.id must beEqualTo(UserFixture.testUser.id)
      user.email must beEqualTo(UserFixture.testUser.email)
      user.avatarURL must beEqualTo(UserFixture.testUser.avatarURL)
    }

    "find user by its Login Info" in {
      // Given
      val userDAO = new UserDAO

      // When
      val user = await(userDAO.findByLoginInfo(UserFixture.testUserLoginInfo)).getOrElse(fail("User cannot be found"))

      // Then
      user.id must beEqualTo(UserFixture.testUser.id)
      user.email must beEqualTo(UserFixture.testUser.email)
      user.avatarURL must beEqualTo(UserFixture.testUser.avatarURL)
    }

    "insert new user" in {
      // Given
      val userDAO = new UserDAO

      val newEmail = Option("new email")
      val newAvatarUrl = Option("new url")
      val newUser = DBUser(None, newEmail, newAvatarUrl)

      // When
      val insertedUser = await(userDAO.insert(newUser))
      val insertedUserId = insertedUser.id.getOrElse(fail("Inserted user does not have id"))
      val foundUser = await(userDAO.findById(insertedUserId)).getOrElse(fail("User cannot be found"))

      // Then
      foundUser.email must beEqualTo(newEmail)
      foundUser.avatarURL must beEqualTo(newAvatarUrl)
    }

    "update a user" in {
      // Given
      val userDAO = new UserDAO

      val updatedEmail = Option("updated email")
      val updatedAvatarUrl = Option("updated url")
      val updatedUser = UserFixture.testUser.copy(email = updatedEmail, avatarURL = updatedAvatarUrl)


      // When
      await(userDAO.update(updatedUser.toDBUser))
      val foundUser = await(userDAO.findById(UserFixture.testUserId)).getOrElse(fail("User cannot be found"))

      // Then
      foundUser.email must beEqualTo(updatedEmail)
      foundUser.avatarURL must beEqualTo(updatedAvatarUrl)
    }
  }


}
