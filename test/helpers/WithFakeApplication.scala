package helpers

import play.api.test.{FakeApplication, WithApplication}
import play.api.test.Helpers._

abstract class WithFakeApplication extends WithApplication(FakeApplication(additionalConfiguration = inMemoryDatabase()))
