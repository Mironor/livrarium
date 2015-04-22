package globals

import com.mohiva.play.silhouette.api.Environment
import com.mohiva.play.silhouette.api.util.PasswordHasher
import com.mohiva.play.silhouette.impl.authenticators.SessionAuthenticator
import com.mohiva.play.silhouette.test.FakeEnvironment
import fixtures.UserFixture
import helpers.{TestPasswordHasher, PseudoUUIDGenerator, RandomIdGenerator}
import models.User
import modules.{SilhouetteModule, WebModule}
import play.api.GlobalSettings
import scaldi.Module
import scaldi.play.{ControllerInjector, ScaldiSupport}

object TestGlobal extends GlobalSettings with ScaldiSupport {

  private val fakeEnvironment = FakeEnvironment[User, SessionAuthenticator](Seq(
    UserFixture.testUserLoginInfo -> UserFixture.testUser)
  )

  def applicationModule = new Module {
    bind[Environment[User, SessionAuthenticator]] to fakeEnvironment
    bind[RandomIdGenerator] to new PseudoUUIDGenerator
    bind[PasswordHasher] to new TestPasswordHasher

  } :: new WebModule :: new SilhouetteModule :: new ControllerInjector

}

