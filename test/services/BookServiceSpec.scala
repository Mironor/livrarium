package services

import fixtures.{BookFixture, FolderFixture, UserFixture}
import helpers.{BookFormatHelper, LivrariumSpecification, RandomIdGenerator}
import models.Book
import org.specs2.matcher.ThrownMessages
import org.specs2.specification.AroundExample

class BookServiceSpec extends LivrariumSpecification with AroundExample with ThrownMessages {


  protected def bootstrapFixtures(): Unit = {
    await(UserFixture.initFixture())
    await(FolderFixture.initFixture())
    await(BookFixture.initFixture())
  }

  "Book Service" should {
    "create new book" in {
      // Given
      val bookService = new BookService
      def randomIdGenerator = inject[RandomIdGenerator]

      val uuid = randomIdGenerator.generateBookId()

      // When
      await(bookService.create(UserFixture.testUser, uuid, "book", BookFormatHelper.PDF))
      val books = await(bookService.retrieveAll(UserFixture.testUser))

      // Then
      books must have size 3
    }

    "update book if it already exists" in {
      // Given
      val bookService = new BookService

      val book = Book.fromDBBook(BookFixture.rootBook)

      val updatedName = "updated book"

      // When
      val updatedBook = await(bookService.save(UserFixture.testUser, book.copy(name = updatedName))).getOrElse(fail("Could not update book (does it belong tu the test user?)"))
      val retrievedBook = await(bookService.retrieve(UserFixture.testUser, updatedBook.id)).getOrElse(fail("Updated book cannot be found"))

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
      val book = await(bookService.retrieve(UserFixture.testUser, BookFixture.otherUserBookId))

      // Then
      book must beNone
    }

    "not save another user's book" in {
      // Given
      val bookService = new BookService

      // When
      val saveReturn = await(bookService.save(UserFixture.testUser, Book.fromDBBook(BookFixture.otherUserBook)))
      val book = await(bookService.retrieve(UserFixture.testUser, BookFixture.otherUserBookId))

      // Then
      saveReturn must beNone
      book must beNone

    }

    "not add another user's book to current user's folder" in {
      // Given
      val bookService = new BookService

      // When
      val saveReturn = await(bookService.save(UserFixture.testUser, Book.fromDBBook(BookFixture.otherUserBook)))
      val book = await(bookService.retrieve(UserFixture.testUser, BookFixture.otherUserBookId))

      // Then
      saveReturn must beNone
      book must beNone

    }

    "not add current user's book to another user's folder" in {
      // Given
      val bookService = new BookService

      // When
      val saveReturn = await(bookService.save(UserFixture.testUser, Book.fromDBBook(BookFixture.otherUserBook)))
      val book = await(bookService.retrieve(UserFixture.testUser, BookFixture.otherUserBookId))

      // Then
      saveReturn must beNone
      book must beNone

    }
  }
}
