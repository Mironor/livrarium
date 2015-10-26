package controllers

import com.mohiva.play.silhouette.impl.authenticators.SessionAuthenticator
import com.mohiva.play.silhouette.test._
import fixtures.{BookFixture, FolderFixture, UserFixture}
import helpers.{BookFormatHelper, LivrariumSpecification}
import models.Book
import org.specs2.matcher.ThrownMessages
import play.api.test._
import scaldi.Injector

class BooksSpec extends LivrariumSpecification with ThrownMessages {

  protected def bootstrapFixtures(implicit inj: Injector): Unit = {
    await(UserFixture.initFixture())
    await(FolderFixture.initFixture())
    await(BookFixture.initFixture())
  }

  "Books controller" should {

    "stream requested book if user owns the book" in { implicit inj: Injector =>
      // Given
      val request = FakeRequest().withAuthenticator[SessionAuthenticator](UserFixture.testUserLoginInfo)

      val booksController = new Books

      // When
      val result = booksController.stream(FolderFixture.sub1Id, BookFixture.sub1BookUUID, BookFormatHelper.PDF)(request)

      // Then
      status(result) mustEqual OK
      headers(result) must havePair("Content-Length" -> "4001")
      headers(result) must havePair(
        "Content-Disposition" -> s"""attachment; filename="${BookFixture.sub1BookUUID}.${BookFormatHelper.PDF}""""
      )
      contentType(result) must beSome("application/pdf")
    }

    "refuse stream requested book if user does not own the folder" in { implicit inj: Injector =>
      // Given
      val request = FakeRequest().withAuthenticator[SessionAuthenticator](UserFixture.testUserLoginInfo)

      val booksController = new Books

      // When
      val result = booksController.stream(FolderFixture.otherUserRootId, BookFixture.sub1BookUUID, BookFormatHelper.PDF)(request)

      // Then
      status(result) mustEqual UNAUTHORIZED
    }

    "refuse stream requested book if user does not own the book" in { implicit inj: Injector =>
      // Given
      val request = FakeRequest().withAuthenticator[SessionAuthenticator](UserFixture.testUserLoginInfo)

      val booksController = new Books

      // When
      val result = booksController.stream(FolderFixture.rootId, BookFixture.otherUserBookUUID, BookFormatHelper.PDF)(request)

      // Then
      status(result) mustEqual UNAUTHORIZED
    }

    "refuse stream requested book if book's format is unsuported" in { implicit inj: Injector =>
      // Given
      val request = FakeRequest().withAuthenticator[SessionAuthenticator](UserFixture.testUserLoginInfo)

      val booksController = new Books

      // When
      val result = booksController.stream(FolderFixture.sub1Id, BookFixture.sub1BookUUID, "Some usuported format")(request)

      // Then
      status(result) mustEqual BAD_REQUEST
    }

    "return a list of all books" in { implicit inj: Injector =>
      // Given
      val request = FakeRequest().withAuthenticator[SessionAuthenticator](UserFixture.testUserLoginInfo)

      val booksController = new Books

      // When
      val result = booksController.all()(request)

      // Then
      status(result) mustEqual OK
      contentType(result) must beSome("application/json")
      val books = contentAsJson(result).as[Seq[Book]]

      books must have size 2
      books(0).id must beEqualTo(BookFixture.rootBookId)
      books(0).name must beEqualTo(BookFixture.rootBookName)
      books(1).id must beEqualTo(BookFixture.sub1BookId)
      books(1).name must beEqualTo(BookFixture.sub1BookName)
    }
  }
}
