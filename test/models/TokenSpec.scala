package models

import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._

import com.mongodb.casbah.commons.MongoDBObject
import org.specs2.specification.BeforeExample

import helpers._

import securesocial.core.providers.Token
import org.joda.time.DateTime


@RunWith(classOf[JUnitRunner])
class TokenSpec extends Specification with BeforeExample {
  val defaultToken = Token("uuid", "email", DateTime.now, DateTime.now, isSignUp = false)

  def before = new WithFakeApplication {
    AuthTokenDAO.remove(MongoDBObject.empty)
    AuthToken.save(defaultToken)
  }

  "User Model" should {

    "be creatable" in new WithFakeApplication {
      AuthToken.all().size must equalTo(1)
    }

    "be searchable by uuid" in new WithFakeApplication {
      val foundToken = AuthToken.find(defaultToken.uuid).getOrElse(None)
      foundToken must equalTo(defaultToken)
    }

    "be deletable" in new WithFakeApplication {
      AuthToken.delete(defaultToken.uuid)

      AuthToken.all().size must equalTo(0)
    }
  }
}
