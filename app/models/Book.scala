package models

import daos.DBTableDefinitions.DBBook
import play.api.libs.functional.syntax._
import play.api.libs.json._

case class Book(id: Long,
                identifier: String,
                name: String,
                format: String,
                totalPages: Int = 0,
                currentPage: Int = 1) {

  def toDBBook(userId: Long): DBBook = DBBook(Option(id), userId, identifier, name, format, totalPages, currentPage)

}

object Book {

  // Implicit conversions for Json Serialisation / Deserialisation
  implicit val bookWrites: Writes[Book] = new Writes[Book] {
    def writes(book: Book) = Json.obj(
      "id" -> book.id,
      "identifier" -> book.identifier,
      "name" -> book.name,
      "format" -> book.format,
      "pages" -> book.totalPages,
      "currentPage" -> book.currentPage
    )
  }

  implicit val bookReads: Reads[Book] = (
    (JsPath \ "id").read[Long] and
      (JsPath \ "identifier").read[String] and
      (JsPath \ "name").read[String] and
      (JsPath \ "format").read[String] and
      (JsPath \ "pages").read[Int] and
      (JsPath \ "currentPage").read[Int]
    )(Book.apply _)


  def fromDBBook(dbBook: DBBook): Book = {
    val id = dbBook.id.getOrElse(throw new Exception(
      """A folder row in the database did not have
        | an id (id field has autoincrement constraint, so it should not be null).
        | Or you are trying to cast a user's row that does not have id (this is strange)""".stripMargin
    ))

    Book(id, dbBook.uuid, dbBook.name, dbBook.format, dbBook.totalPages, dbBook.currentPage)
  }
}

