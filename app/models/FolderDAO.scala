package models

import models.DBTableDefinitions.{DBFolder, Folders}
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick._
import scala.slick.jdbc.JdbcBackend.Session

import scala.concurrent.Future
import scala.slick.lifted.TableQuery

import play.api.Play.current

class FolderDAO {

  val slickFolders = TableQuery[Folders]

  def findUserFolders(user: User): Future[List[DBFolder]] = {
    Future.successful {
      DB withSession { implicit session =>
        slickFolders.filter(folder => folder.idUser === user.id.get).sortBy(_.left.desc)
          .list
      }
    }
  }

  def createRootForUser(user: User)(implicit session: Session): Unit = {
    slickFolders += DBFolder(None, user.id.get, "_root_", 0, 1)
  }
}
