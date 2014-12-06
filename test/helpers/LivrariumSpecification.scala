package helpers

import com.mohiva.play.silhouette.api.Environment
import com.mohiva.play.silhouette.impl.authenticators.SessionAuthenticator
import globals.TestGlobal
import play.api.test.PlaySpecification
import scaldi.{Injectable, Injector}
import services.User

class LivrariumSpecification extends PlaySpecification with Injectable{

  implicit def injector: Injector = TestGlobal.injector

  implicit def env: Environment[User, SessionAuthenticator] = inject[Environment[User, SessionAuthenticator]]

}
