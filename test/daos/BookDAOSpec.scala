package daos

import daos.DBTableDefinitions.DBBook
import fixtures.{BookFixture, FolderFixture, UserFixture}
import helpers.{BookFormatHelper, LivrariumSpecification, RandomIdGenerator}
import org.specs2.matcher.ThrownMessages
import org.specs2.specification.AroundEach

class BookDAOSpec extends LivrariumSpecification with AroundEach with ThrownMessages {

  protected def bootstrapFixtures(): Unit = {
    await(UserFixture.initFixture())
    await(FolderFixture.initFixture())
    await(BookFixture.initFixture())
  }

  "Book DAO" should {
    "insert a book" in {
      skipped("")
      // Given
      val bookDAO = new BookDAO

      val book = generateTestBook()

      // When
      val insertedBook = await(bookDAO.insert(book))
      val insertedBookId = insertedBook.id.getOrElse(fail("Inserted book has no id"))
      val books = await(bookDAO.findAll(UserFixture.testUserId))

      val testBook = await(bookDAO.findById(insertedBookId)).getOrElse(fail("Inserted book was not found"))

      // Then
      books must have size 3

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
      skipped("")
      // Given
      val bookDAO = new BookDAO

      val updatedName = "updated book"

      val book = generateTestBook()

      // When
      val insertedBook = await(bookDAO.insert(book))
      val insertedBookId = insertedBook.id.getOrElse(fail("Inserted book has no id"))
      await(bookDAO.update(insertedBook.copy(name = updatedName)))

      val testBook = await(bookDAO.findById(insertedBookId)).getOrElse(fail("Inserted/updated book was not found"))

      // Then
      testBook.name must beEqualTo(updatedName)
    }

    "relate book to a folder" in {
      skipped("")
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
