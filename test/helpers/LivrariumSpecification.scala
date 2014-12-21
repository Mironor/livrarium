package helpers

import com.mohiva.play.silhouette.api.Environment
import com.mohiva.play.silhouette.impl.authenticators.SessionAuthenticator
import globals.TestGlobal
import models.User
import play.api.test.PlaySpecification
import scaldi.{Injectable, Injector}

class LivrariumSpecification extends PlaySpecification with Injectable{

  implicit def injector: Injector = TestGlobal.injector

  implicit def env: Environment[User, SessionAuthenticator] = inject[Environment[User, SessionAuthenticator]]

}
