package globals

import com.mohiva.play.silhouette.api.{Environment, LoginInfo}
import com.mohiva.play.silhouette.impl.authenticators.SessionAuthenticator
import com.mohiva.play.silhouette.test.FakeEnvironment
import helpers.{PseudoUUIDGenerator, RandomIdGenerator}
import modules.{SilhouetteModule, WebModule}
import play.api.GlobalSettings
import scaldi.Module
import scaldi.play.{ControllerInjector, ScaldiSupport}
import services.User

object TestGlobal extends GlobalSettings with ScaldiSupport {
  val testUser = User(Option(1), LoginInfo("key", "value"), None, None)

  def applicationModule = new Module {
    bind[Environment[User, SessionAuthenticator]] to FakeEnvironment[User, SessionAuthenticator](testUser)
    bind[RandomIdGenerator] to new PseudoUUIDGenerator

  } :: new WebModule :: new SilhouetteModule :: new ControllerInjector

}

