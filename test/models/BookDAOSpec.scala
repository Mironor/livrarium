package models

import globals.TestGlobal
import helpers.{BookFormatHelper, RandomIdGenerator, LivrariumSpecification}
import models.DBTableDefinitions.DBBook
import org.specs2.execute.AsResult
import org.specs2.matcher.ThrownMessages
import org.specs2.specification.AroundExample
import play.api.test.FakeApplication
import services.UserService

class BookDAOSpec extends LivrariumSpecification with AroundExample with ThrownMessages {

  def randomIdGenerator = inject[RandomIdGenerator]

  def around[T: AsResult](t: => T) = {
    val app = FakeApplication(withGlobal = Some(TestGlobal), additionalConfiguration = inMemoryDatabase())
    running(app) {
      val userService = inject[UserService]
      await(userService.saveWithLoginInfo(TestGlobal.testUser))
      prepareTestBookModels()

      AsResult(t)
    }
  }

  def prepareTestBookModels() = {

    val bookDAO = new BookDAO
    val userId = TestGlobal.testUser.id.getOrElse(fail("User's id is not defined"))
    val book = DBBook(
      None,
      userId,
      randomIdGenerator.generateBookId(),
      "book",
      BookFormatHelper.PDF
    )

    bookDAO.insertOrUpdate(book)
  }

  "Book DAO" should {
    "insert a book" in {
      // Given
      val bookDAO = new BookDAO

      // When
      val books = await(bookDAO.findAll(TestGlobal.testUser))

      // Then
      books must have size 1

      val insertedBook = books(0)
      val userId = TestGlobal.testUser.id.getOrElse(fail("User's id is not defined"))
      insertedBook.userId must beEqualTo(userId)
      insertedBook.name must beEqualTo("book")
      insertedBook.format must beEqualTo(BookFormatHelper.PDF)
    }

    /*
    "relate book to a folder" in {
      // Given
      val bookDAO = new BookDAO

      // When

    }
    */
  }
}
