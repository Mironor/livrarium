package helpers

import play.api.test.{FakeApplication, WithApplication}
import play.api.test.Helpers._


object TestConfigurationHelper {
  lazy val fakeApplication = FakeApplication(
    additionalConfiguration = inMemoryDatabase() /*++ Map(
      "silhouette.authenticator.sessionKey" -> "livrarium-auth-test",
      "silhouette.authenticator.encryptAuthenticator" -> true,
      "silhouette.authenticator.useFingerprinting" -> true,
      "silhouette.authenticator.authenticatorIdleTimeout" -> 1800,
      "silhouette.authenticator.authenticatorExpiry" -> 43200
    )*/
  )
}

