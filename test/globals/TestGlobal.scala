package globals

import com.mohiva.play.silhouette.api.Environment
import com.mohiva.play.silhouette.impl.authenticators.SessionAuthenticator
import com.mohiva.play.silhouette.test.FakeEnvironment
import fixtures.UserFixture
import helpers.{PseudoUUIDGenerator, RandomIdGenerator}
import models.User
import modules.{SilhouetteModule, WebModule}
import play.api.GlobalSettings
import scaldi.Module
import scaldi.play.{ControllerInjector, ScaldiSupport}

object TestGlobal extends GlobalSettings with ScaldiSupport {

  def applicationModule = new Module {
    bind[Environment[User, SessionAuthenticator]] to FakeEnvironment[User, SessionAuthenticator](UserFixture.testUser)
    bind[RandomIdGenerator] to new PseudoUUIDGenerator

  } :: new WebModule :: new SilhouetteModule :: new ControllerInjector

}

