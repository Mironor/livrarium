package helpers

import play.api.test.{FakeApplication, WithApplication}

abstract class WithFakeApplication extends WithApplication(FakeApplication(additionalConfiguration = Map(
  "mongodb.default.db" -> "livrarium_test"
)))
