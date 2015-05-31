package models

import java.util.UUID

import daos.DBTableDefinitions.DBBook
import play.api.libs.json._
import play.api.libs.functional.syntax._

case class Book(id: Long,
                identifier: UUID,
                name: String,
                format: String,
                totalPages: Int = 0,
                currentPage: Int = 0) {

  def toDBBook(userId: Long): DBBook = DBBook(Option(id), userId, identifier, name, format, totalPages, currentPage)

}

object Book {

  // Implicit conversions for Json Serialisation / Deserialisation
  implicit val bookWrites: Writes[Book] = (
    (__ \ "id").write[Long] and
      (__ \ "identifier").write[UUID] and
      (__ \ "name").write[String] and
      (__ \ "format").write[String] and
      (__ \ "totalPates").write[Int] and
      (__ \ "currentPage").write[Int]
    )(unlift(Book.unapply))


  def fromDBBook(dbBook: DBBook): Book = {
    val id = dbBook.id.getOrElse(throw new Exception(
      """A folder row in the database did not have
        | an id (id field has autoincrement constraint, so it should not be null).
        | Or you are trying to cast a user's row that does not have id (this is strange)""".stripMargin
    ))

    Book(id, dbBook.uuid, dbBook.name, dbBook.format, dbBook.totalPages, dbBook.currentPage)
  }
}

