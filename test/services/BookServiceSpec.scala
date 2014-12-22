package services

import fixtures.{BookFixture, UserFixture, FolderFixture}
import globals.TestGlobal
import helpers.{BookFormatHelper, RandomIdGenerator, LivrariumSpecification}
import models.Book
import org.specs2.execute.AsResult
import org.specs2.matcher.ThrownMessages
import org.specs2.specification.AroundExample
import play.api.test.FakeApplication

class BookServiceSpec extends LivrariumSpecification with AroundExample with ThrownMessages {


  /**
   * This automatically handles up and down evolutions at the beginning and at the end of a spec respectively
   */
  def around[T: AsResult](t: => T) = {
    val app = FakeApplication(withGlobal = Some(TestGlobal), additionalConfiguration = inMemoryDatabase())
    running(app) {
      await(UserFixture.initFixture())
      await(FolderFixture.initFixture())
      await(BookFixture.initFixture())

      AsResult(t)
    }
  }

  "Book Service" should {
    "create new book" in {
      // Given
      val bookService = new BookService

      val book = generateTestBook()

      // When
      await(bookService.save(UserFixture.testUser, book))
      val books = await(bookService.retrieveAll(UserFixture.testUser))

      // Then
      books must have size 3
    }

    def generateTestBook(): Book = {
      def randomIdGenerator = inject[RandomIdGenerator]

      Book(
        None,
        randomIdGenerator.generateBookId(),
        "book",
        BookFormatHelper.PDF
      )
    }

    "update book if it already exists" in {
      // Given
      val bookService = new BookService

      val book = generateTestBook()

      val updatedName = "updated book"

      // When
      val insertedBook = await(bookService.save(UserFixture.testUser, book))
      val updatedBook = await(bookService.save(UserFixture.testUser, insertedBook.copy(name = updatedName)))
      val updatedBookId = updatedBook.id.getOrElse(fail("Updated book has no id"))
      val retrievedBookOption = await(bookService.retrieveById(UserFixture.testUser, updatedBookId))
      val retrievedBook = retrievedBookOption.getOrElse(fail("Updated book cannot be found"))

      // Then
      retrievedBook.name must equalTo(updatedName)
    }

    "retrieve all books from a folder" in {
      // Given
      val bookService = new BookService

      val book = generateTestBook()

      // When
      val insertedBook = await(bookService.save(UserFixture.testUser, book))
      await(bookService.addToFolder(UserFixture.testUser, insertedBook, FolderFixture.sub2Id))
      val books = await(bookService.retrieveAllFromFolder(UserFixture.testUser, FolderFixture.sub2Id))

      // Then
      books must have size 1
    }
  }


}
