package modules

import com.mohiva.play.silhouette.api.Environment
import com.mohiva.play.silhouette.api.util.PasswordHasher
import com.mohiva.play.silhouette.impl.authenticators.SessionAuthenticator
import com.mohiva.play.silhouette.test.FakeEnvironment
import fixtures.UserFixture
import helpers._
import models.User
import scaldi.Module

import play.api.libs.concurrent.Execution.Implicits.defaultContext

class SpecModule extends Module{
  bind[Environment[User, SessionAuthenticator]] to FakeEnvironment[User, SessionAuthenticator](Seq(
    UserFixture.testUserLoginInfo -> UserFixture.testUser)
  )
  bind[RandomIdGenerator] to new PseudoUUIDGenerator
  bind[FileHelper] to new FileTestHelper
  bind[PasswordHasher] to new PasswordTestHasher
}
