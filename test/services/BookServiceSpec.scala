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

      val book = Book.fromDBBook(BookFixture.rootBook)

      val updatedName = "updated book"

      // When
      val updatedBookOption = await(bookService.save(UserFixture.testUser, book.copy(name = updatedName)))
      val updatedBook = updatedBookOption.getOrElse(fail("Updated book was not saved"))
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
      val insertedBookOption = await(bookService.save(UserFixture.testUser, book))
      val insertedBook = insertedBookOption.getOrElse(fail("Inserted book was not saved"))
      await(bookService.addToFolder(UserFixture.testUser, insertedBook, FolderFixture.sub2Id))
      val books = await(bookService.retrieveAllFromFolder(UserFixture.testUser, FolderFixture.sub2Id))

      // Then
      books must have size 1
    }

    "retrieve None if trying to retrieve another user's book" in {
      // Given
      val bookService = new BookService

      // When
      val book = await(bookService.retrieveById(UserFixture.testUser, BookFixture.otherUserBookId))

      // Then
      book must beNone
    }

    "not be able to save another user's book" in {
      // Given
      val bookService = new BookService

      // When
      val saveReturn = await(bookService.save(UserFixture.testUser, Book.fromDBBook(BookFixture.otherUserBook)))
      val book = await(bookService.retrieveById(UserFixture.testUser, BookFixture.otherUserBookId))

      // Then
      saveReturn must beNone
      book must beNone

    }
  }
}
