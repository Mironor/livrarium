package models

import models.DBTableDefinitions.{DBFolder, Folders}
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick._
import services.User

import scala.concurrent.Future
import scala.slick.lifted.TableQuery
import scala.slick.jdbc.StaticQuery.interpolation

import play.api.Play.current

class FolderDAO {

  val rootFolderName = "__root__"
  val slickFolders = TableQuery[Folders]

  /**
   * Gets user's root folder
   * @param user User
   * @return
   */
  def findUserRoot(user: User): Future[Option[DBFolder]] = {
    Future.successful {
      DB withSession { implicit session =>
        slickFolders.filter(folder => folder.idUser === user.id.get && folder.left === 0)
          .firstOption
      }
    }
  }

  /**
   * Gets folder by Id
   * @param user user
   * @param folderId folder's id
   * @return
   */
  def findById(user: User, folderId: Long): Future[Option[DBFolder]] = {
    Future.successful {
      DB withSession { implicit session =>
        slickFolders.filter(folder => folder.idUser === user.id.get && folder.id === folderId)
          .firstOption
      }
    }
  }

  /**
   * Gets all user's folders ordered by `left` field (see Nested Sets pattern)
   * @param user User
   * @return
   */
  def findUserFolders(user: User): Future[List[DBFolder]] = {
    Future.successful {
      DB withSession { implicit session =>
        slickFolders.filter(folder => folder.idUser === user.id.get).sortBy(_.left.asc)
          .list
      }
    }
  }

  /**
   * Creates root folder for supplied User
   * @param user User
   * @return
   */
  def createRootForUser(user: User): Future[_] = {
    Future.successful {
      DB withSession { implicit session =>
        slickFolders += DBFolder(None, user.id.get, rootFolderName, 0, 1)
      }
    }
  }

  /**
   * Appends sub folder to user's folder
   * @param user User
   * @param folderParent parent folder
   * @param folderName new folder's name
   * @return
   */
  def appendToFolder(user: User, folderParent: DBFolder, folderName: String): Future[DBFolder] = {
    Future.successful {
      DB withSession { implicit session =>

        val userId = user.id.getOrElse(throw IllegalArgumentException)
        val parentRight = folderParent.right

        // Making space in parents Folders (and folders to the right of the parent folder
        // Slick does not support mutating updates so we will use plain query here
        sqlu"""UPDATE "folders" SET "right" = "right" + 2 where "idUser" = $userId AND "right" >= $parentRight""".execute
        sqlu"""UPDATE "folders" SET "left" = "left" + 2 where "idUser" = $userId AND "left" > $parentRight""".execute

        val appendedDBFolder = DBFolder(None, user.id.get, folderName, folderParent.right, folderParent.right + 1)
        val appendedDBFolderId = (slickFolders returning slickFolders.map(_.id)) += appendedDBFolder
        appendedDBFolder.copy(id = Option(appendedDBFolderId))
      }
    }
  }
}
