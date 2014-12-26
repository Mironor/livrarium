package models

import daos.{BookDAO, DBTableDefinitions}
import fixtures.{BookFixture, FolderFixture, UserFixture}
import globals.TestGlobal
import helpers.{BookFormatHelper, LivrariumSpecification, RandomIdGenerator}
import DBTableDefinitions.DBBook
import org.specs2.execute.AsResult
import org.specs2.matcher.ThrownMessages
import org.specs2.specification.AroundExample
import play.api.test.FakeApplication

class BookDAOSpec extends LivrariumSpecification with AroundExample with ThrownMessages {

  def randomIdGenerator = inject[RandomIdGenerator]

  def around[T: AsResult](t: => T) = {
    val app = FakeApplication(withGlobal = Some(TestGlobal), additionalConfiguration = inMemoryDatabase())
    running(app) {
      await(UserFixture.initFixture())
      await(FolderFixture.initFixture())
      await(BookFixture.initFixture())

      AsResult(t)
    }
  }

  "Book DAO" should {
    "insert a book" in {
      // Given
      val bookDAO = new BookDAO

      val book = generateTestBook()

      // When
      val insertedBook = await(bookDAO.insert(book))
      val insertedBookId = insertedBook.id.getOrElse(fail("Inserted book has no id"))
      val books = await(bookDAO.findAll(UserFixture.testUserId))

      val testBookOption = await(bookDAO.findById(insertedBookId))

      // Then
      books must have size 3

      val testBook = testBookOption.getOrElse(fail("Inserted book was not found"))
      testBook.idUser must beEqualTo(UserFixture.testUserId)
      testBook.name must beEqualTo(book.name)
      testBook.format must beEqualTo(BookFormatHelper.PDF)
    }

    def generateTestBook(): DBBook = {
      val randomIdGenerator = inject[RandomIdGenerator]
      val generatedBookId = randomIdGenerator.generateBookId() // it generates the same id each time due to the injection

      DBBook(
        None,
        UserFixture.testUserId,
        generatedBookId,
        "book",
        BookFormatHelper.PDF
      )
    }

    "update book if it already exists" in {
      // Given
      val bookDAO = new BookDAO

      val updatedName = "updated book"

      val book = generateTestBook()

      // When
      val insertedBook = await(bookDAO.insert(book))
      val insertedBookId = insertedBook.id.getOrElse(fail("Inserted book has no id"))
      await(bookDAO.update(insertedBook.copy(name = updatedName)))

      val testBookOption = await(bookDAO.findById(insertedBookId))

      // Then
      val testBook = testBookOption.getOrElse(fail("Inserted/updated book was not found"))
      testBook.name must beEqualTo(updatedName)
    }

    "relate book to a folder" in {
      // Given
      val bookDAO = new BookDAO

      val book = generateTestBook()

      // When
      val insertedBook = await(bookDAO.insert(book))
      val insertedBookId = insertedBook.id.getOrElse(fail("Inserted book's id is not available"))
      await(bookDAO.relateBookToFolder(insertedBookId, FolderFixture.sub2Id))
      val books = await(bookDAO.findAllInFolder(FolderFixture.sub2Id))

      // Then
      books must have size 1

    }


  }
}
