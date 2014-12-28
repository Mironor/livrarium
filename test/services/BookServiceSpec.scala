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
      val updatedBook = await(bookService.save(UserFixture.testUser, book.copy(name = updatedName))).getOrElse(fail("Updated book was not saved"))
      val updatedBookId = updatedBook.id.getOrElse(fail("Updated book has no id"))
      val retrievedBook = await(bookService.retrieveById(UserFixture.testUser, updatedBookId)).getOrElse(fail("Updated book cannot be found"))

      // Then
      retrievedBook.name must equalTo(updatedName)
    }

    "retrieve all books from a folder" in {
      // Given
      val bookService = new BookService

      // When
      val books = await(bookService.retrieveAllFromFolder(UserFixture.testUser, FolderFixture.sub1Id))
      val noBooks = await(bookService.retrieveAllFromFolder(UserFixture.testUser, FolderFixture.sub2Id))

      // Then
      books must have size 1
      noBooks must beEmpty
    }

    "not retrieve books from other user's folder" in {
      // Given
      val bookService = new BookService

      // When
      val books = await(bookService.retrieveAllFromFolder(UserFixture.testUser, FolderFixture.otherUserRootId))

      // Then
      books must beEmpty
    }

    "retrieve None if trying to retrieve another user's book" in {
      // Given
      val bookService = new BookService

      // When
      val book = await(bookService.retrieveById(UserFixture.testUser, BookFixture.otherUserBookId))

      // Then
      book must beNone
    }

    "not save another user's book" in {
      // Given
      val bookService = new BookService

      // When
      val saveReturn = await(bookService.save(UserFixture.testUser, Book.fromDBBook(BookFixture.otherUserBook)))
      val book = await(bookService.retrieveById(UserFixture.testUser, BookFixture.otherUserBookId))

      // Then
      saveReturn must beNone
      book must beNone

    }

    "return None/Nil if user's id is not supplied" in {
      // Given
      val bookService = new BookService

      val userWithoutId = UserFixture.testUser.copy(id = None)
      val testBook = Book.fromDBBook(BookFixture.rootBook)
      val testFolderId = FolderFixture.rootId

      // When
      val retrieveAll = await(bookService.retrieveAll(userWithoutId))
      val retrieveById = await(bookService.retrieveById(userWithoutId, testFolderId))
      val save = await(bookService.save(userWithoutId, testBook))
      val addToFolder = await(bookService.addToFolder(userWithoutId, testBook, testFolderId))
      val retrieveAllFromFolder = await(bookService.retrieveAllFromFolder(userWithoutId, testFolderId))

      // Then
      retrieveAll must beEmpty
      retrieveById must beNone
      save must beNone
      addToFolder must beNone
      retrieveAllFromFolder must beEmpty
    }

    "not add another user's book to current user's folder" in {
      // Given
      val bookService = new BookService

      // When
      val saveReturn = await(bookService.save(UserFixture.testUser, Book.fromDBBook(BookFixture.otherUserBook)))
      val book = await(bookService.retrieveById(UserFixture.testUser, BookFixture.otherUserBookId))

      // Then
      saveReturn must beNone
      book must beNone

    }

    "not add current user's book to another user's folder" in {
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
