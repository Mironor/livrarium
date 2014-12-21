package daos

import daos.DBTableDefinitions.{DBFolder, Folders}
import models.User
import play.api.Play.current
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick._

import scala.concurrent.Future
import scala.slick.jdbc.JdbcBackend.Session
import scala.slick.jdbc.StaticQuery.interpolation
import scala.slick.lifted.TableQuery

class FolderDAO {

  val rootFolderName = "__root__"
  val slickFolders = TableQuery[Folders]

  /**
   * Gets user's root folder
   * @param user User
   * @return
   */
  def findRoot(user: User): Future[Option[DBFolder]] = {
    Future.successful {
      DB withSession { implicit session =>
        val userId = user.id.getOrElse(throw new DAOException("User.id should be defined"))
        slickFolders.filter(folder => folder.idUser === userId && folder.left === 0)
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
        val userId = user.id.getOrElse(throw new DAOException("User.id should be defined"))
        slickFolders.filter(folder => folder.idUser === userId && folder.id === folderId)
          .firstOption
      }
    }
  }

  /**
   * Gets all user's folders ordered by `left` field (see Nested Sets pattern)
   * @param user User
   * @return
   */
  def findAll(user: User): Future[List[DBFolder]] = {
    Future.successful {
      DB withSession { implicit session =>
        val userId = user.id.getOrElse(throw new DAOException("User.id should be defined"))
        slickFolders.filter(folder => folder.idUser === userId).sortBy(_.left.asc)
          .list
      }
    }
  }

  /**
   * Find folder's immediate children
   * @param user current user
   * @param folderId parent's folder id
   * @return
   */
  def findChildrenById(user: User, folderId: Long): Future[List[DBFolder]] = {
    Future.successful {
      DB withSession { implicit session =>
        val userId = user.id.getOrElse(throw new DAOException("User.id should be defined"))
        val parentFolderOption = slickFolders.filter(folder => folder.idUser === userId && folder.id === folderId).firstOption
        val parentFolder = parentFolderOption.getOrElse(throw new DAOException("Parent folder cannot be found"))


        slickFolders.filter(child =>
          child.idUser === userId
            && child.left > parentFolder.left
            && child.left > parentFolder.left
            && child.right < parentFolder.right
            && child.level === (parentFolder.level + 1)
        ).list
      }
    }
  }

  /**
   * findChildren proxy
   * @param user current user
   * @param folder parent's folder dto
   * @return
   */
  def findChildren(user: User, folder: DBFolder): Future[List[DBFolder]] = {
    val folderId = folder.id.getOrElse(throw new DAOException("Parent folder's id should be defined"))
    findChildrenById(user, folderId)
  }

  /**
   * Creates root folder for supplied User
   * @param user User
   * @return
   */
  def insertRoot(user: User): Future[DBFolder] = {
    Future.successful {
      DB withSession { implicit session =>
        val userId = user.id.getOrElse(throw new DAOException("User.id should be defined"))
        val rootFolder = DBFolder(None, userId, rootFolderName, 0, 0, 1)
        val rootFolderId = (slickFolders returning slickFolders.map(_.id)) += rootFolder
        rootFolder.copy(id = Option(rootFolderId))
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

        val userId = user.id.getOrElse(throw new DAOException("User.id should be defined"))
        val parentRight = folderParent.right

        updateOthers(userId, parentRight)

        val appendedDBFolder = DBFolder(None, userId, folderName, folderParent.level + 1, folderParent.right, folderParent.right + 1)
        val appendedDBFolderId = (slickFolders returning slickFolders.map(_.id)) += appendedDBFolder
        appendedDBFolder.copy(id = Option(appendedDBFolderId))
      }
    }
  }

  private def updateOthers(userId: Long, parentRight: Int)(implicit session: Session) {
    // Making space in parents Folders (and folders to the right of the parent folder
    // Slick does not support mutating updates so we will use plain query
    // IMPORTANT: exceptions are silent, so make sure to treat them
    try {
      sqlu"""UPDATE "folders" SET "right" = "right" + 2 where "idUser" = $userId AND "right" >= $parentRight""".execute
      sqlu"""UPDATE "folders" SET "left" = "left" + 2 where "idUser" = $userId AND "left" > $parentRight""".execute
    } catch {
      case e: Exception => throw new DAOException("Append folder: slick could not execute plain query")
    }
  }
}
