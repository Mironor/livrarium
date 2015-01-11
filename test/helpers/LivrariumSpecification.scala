package helpers

import com.mohiva.play.silhouette.api.Environment
import com.mohiva.play.silhouette.impl.authenticators.SessionAuthenticator
import globals.TestGlobal
import models.User
import org.specs2.execute.AsResult
import play.api.test.{FakeApplication, PlaySpecification}
import scaldi.{Injectable, Injector}

abstract class LivrariumSpecification extends PlaySpecification with Injectable {

  implicit def injector: Injector = TestGlobal.injector

  implicit def env: Environment[User, SessionAuthenticator] = inject[Environment[User, SessionAuthenticator]]


  /**
   * This automatically handles up and down evolutions at the beginning and at the end of a spec respectively
   */
  def around[T: AsResult](t: => T) = {
    val app = FakeApplication(withGlobal = Some(TestGlobal), additionalConfiguration = inMemoryDatabase())
    running(app) {
      bootstrapFixtures()
      AsResult(t)
    }
  }

  protected def bootstrapFixtures(): Unit
}
