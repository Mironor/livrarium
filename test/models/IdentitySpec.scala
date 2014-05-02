package models

import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._

import com.mongodb.casbah.commons.MongoDBObject
import org.specs2.specification.BeforeExample

import helpers._
import org.bson.types.ObjectId

import securesocial.core.{SocialUser, AuthenticationMethod, IdentityId}


@RunWith(classOf[JUnitRunner])
class IdentitySpec extends Specification with BeforeExample {
  val defaultIdentity = SocialUser(IdentityId("id", "provider"), "Alex", "B", "Alex B.", Some("alex@git.com"), None, AuthenticationMethod("method"), None, None, None)

  def before = new WithFakeApplication {
    IdentityDAO.remove(MongoDBObject.empty)
    Identity.save(defaultIdentity)
  }

  "User Model" should {

    "be creatable" in new WithFakeApplication {
      IdentityDAO.find(MongoDBObject.empty).size must equalTo(1)
    }

    "be searchable by IdentityId" in new WithFakeApplication {
      val foundIdentity = Identity.find(defaultIdentity.identityId).getOrElse(None)
      foundIdentity must equalTo(defaultIdentity)
    }

    "be searchable by email and provider" in new WithFakeApplication {
      val foundIdentity = Identity.findByEmailAndProvider(
        defaultIdentity.email.getOrElse(""),
        defaultIdentity.identityId.providerId
      ).getOrElse(None)

      foundIdentity must equalTo(defaultIdentity)
    }

    "be deletable" in new WithFakeApplication {
      Identity.delete(defaultIdentity.identityId)

      IdentityDAO.find(MongoDBObject.empty).size must equalTo(0)
    }
  }
}
