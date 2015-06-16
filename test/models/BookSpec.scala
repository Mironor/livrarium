package models

import java.util.UUID

import daos.DBTableDefinitions.DBBook
import helpers.BookFormatHelper
import play.api.test.PlaySpecification

class BookSpec extends PlaySpecification{

  "Book model" should {
    "be creatable from dbBook" in {
      // Given
      val uuid = UUID.fromString("00000000-0000-4000-A000-000000000000").toString
      val bookName = "book"
      val totalPages = 100
      val currentPage = 42
      val dbBook = DBBook(Option(1), 1, uuid,bookName, BookFormatHelper.PDF, totalPages, currentPage)

      // When
      val book = Book.fromDBBook(dbBook)

      // Then
      book.id must beEqualTo(1)
      book.identifier must beEqualTo(uuid)
      book.format must beEqualTo(BookFormatHelper.PDF)
      book.name must beEqualTo(bookName)
      book.totalPages must beEqualTo(totalPages)
      book.currentPage must beEqualTo(currentPage)
    }

    "throw an exception if created from dbBook that has no id" in {
      // Given
      val uuid = UUID.fromString("00000000-0000-4000-A000-000000000000").toString
      val dbBook = DBBook(None, 1, uuid,"", BookFormatHelper.PDF)

      // When // Then
      Book.fromDBBook(dbBook) must throwA[Exception]
    }

    "be castable in dbBook" in {
      // Given
      val uuid = UUID.fromString("00000000-0000-4000-A000-000000000000").toString
      val bookName = "book"
      val totalPages = 100
      val currentPage = 42
      val userId = 1
      val book = Book(1, uuid,bookName, BookFormatHelper.PDF, totalPages, currentPage)

      // When
      val dbBook = book.toDBBook(userId)

      // Then
      dbBook.id must beSome(1)
      dbBook.uuid must beEqualTo(uuid)
      dbBook.format must beEqualTo(BookFormatHelper.PDF)
      dbBook.name must beEqualTo(bookName)
      dbBook.totalPages must beEqualTo(totalPages)
      dbBook.currentPage must beEqualTo(currentPage)
    }
  }
}
