package models

import java.util.UUID

import daos.DBTableDefinitions.DBBook

case class Book(id: Long,
                identifier: UUID,
                name: String,
                format: String,
                totalPages: Int = 0,
                currentPage: Int = 0) {

  def toDBBook(userId: Long): DBBook = DBBook(Option(id), userId, identifier, name, format, totalPages, currentPage)

}

object Book {
  def fromDBBook(dbBook: DBBook): Book = {
    val id = dbBook.id.getOrElse(throw new Exception(
      """A folder row in the database did not have
        | an id (id field has autoincrement constraint, so it should not be null).
        | Or you are trying to cast a user's row that does not have id (this is strange)""".stripMargin
    ))

    Book(id, dbBook.uuid, dbBook.name, dbBook.format, dbBook.totalPages, dbBook.currentPage)
  }
}

