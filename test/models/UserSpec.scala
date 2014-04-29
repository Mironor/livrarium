package models

import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._

import com.mongodb.casbah.commons.MongoDBObject
import org.specs2.specification.BeforeExample

import helpers._
import org.bson.types.ObjectId

import securesocial.core.{AuthenticationMethod, IdentityId}


@RunWith(classOf[JUnitRunner])
class UserSpec extends Specification with BeforeExample {
  val defaultUser = User(new ObjectId, IdentityId("id", "provider"), "Alex", "B", "Alex B.", None, None, AuthenticationMethod("method"), None, None, None)

  def before = new WithFakeApplication {
    UserDAO.remove(MongoDBObject.empty)
    User.save(defaultUser)
  }

  "User Model" should {

    "be creatable" in new WithFakeApplication {
      User.all().size must equalTo(1)
    }

    "be searchable by IdentityId" in new WithFakeApplication {
      val foundUser = User.find(defaultUser.identityId).getOrElse(None)
      foundUser must equalTo(defaultUser)
    }

    "be deletable" in new WithFakeApplication {
      User.delete(defaultUser.identityId)

      User.all().size must equalTo(0)
    }
  }
}
