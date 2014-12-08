package models

import models.DBTableDefinitions.{DBBook, Books}
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick._
import services.User
import play.api.Play.current

import scala.concurrent.Future
import scala.slick.lifted.TableQuery

class BookDAO {
  val slickBooks = TableQuery[Books]

  def findAll(user: User): Future[List[DBBook]] = {
    Future.successful {
      DB withSession { implicit session =>
        val userId = user.id.getOrElse(throw new DAOException("User.id should be defined"))
        slickBooks.filter(_.idUser === userId).list
      }
    }
  }

  def insertOrUpdate(book: DBBook): Future[DBBook] = {
    Future.successful {
      DB withSession { implicit session =>
        val insertedBookId = (slickBooks returning slickBooks.map(_.id)) += book
        book.copy(id = Option(insertedBookId))
      }
    }
  }
}
