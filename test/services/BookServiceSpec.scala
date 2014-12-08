package services

import globals.TestGlobal
import helpers.{BookFormatHelper, RandomIdGenerator, LivrariumSpecification}
import org.specs2.execute.AsResult
import org.specs2.matcher.ThrownMessages
import org.specs2.specification.AroundExample
import play.api.test.FakeApplication

class BookServiceSpec extends LivrariumSpecification with AroundExample with ThrownMessages {

  def randomIdGenerator = inject[RandomIdGenerator]

  /**
   * This automatically handles up and down evolutions at the beginning and at the end of a spec respectively
   */
  def around[T: AsResult](t: => T) = {
    val app = FakeApplication(withGlobal = Some(TestGlobal), additionalConfiguration = inMemoryDatabase())
    running(app) {
      val userService = inject[UserService]
      await(userService.saveWithLoginInfo(TestGlobal.testUser))
      prepareTestBooks()

      AsResult(t)
    }
  }

  def prepareTestBooks() = {
    val bookService = new BookService

    val book = Book(
      None,
      randomIdGenerator.generateBookId(),
      "book",
      BookFormatHelper.PDF
    )

    bookService.save(TestGlobal.testUser, book)
  }

  "Book Service" should {
    "create new book" in {
      // Given
      val bookService = new BookService

      // When
      val books = await(bookService.retrieveAll(TestGlobal.testUser))

      // Then
      books must have size 1
    }
  }


}
