package models

import java.util.UUID

import daos.DBTableDefinitions.DBBook

case class Book(id: Option[Long],
                identifier: UUID,
                name: String,
                format: String,
                totalPages: Int = 0,
                currentPage: Int = 0) {

  def toDBBook(userId: Long): DBBook = DBBook(
    id,
    userId,
    identifier,
    name,
    format,
    totalPages,
    currentPage
  )

}

object Book {
  def fromDBBook(dbBook: DBBook): Book = Book(
    dbBook.id,
    dbBook.uuid,
    dbBook.name,
    dbBook.format,
    dbBook.totalPages,
    dbBook.currentPage
  )
}

