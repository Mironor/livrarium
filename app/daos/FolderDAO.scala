package daos

import daos.DBTableDefinitions.{DBFolder, Folders}
import play.api.Play.current
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick._

import scala.concurrent.Future
import scala.slick.jdbc.JdbcBackend.Session
import scala.slick.jdbc.StaticQuery.interpolation
import scala.slick.lifted.TableQuery

class FolderDAO {

  val rootFolderName = ""
  val slickFolders = TableQuery[Folders]

  /**
   * Gets all user's folders ordered by `left` field (see Nested Sets pattern)
   * @param userId current user's id
   * @return a promise of a list of user's folders ordered by `left` field
   */
  def findAll(userId: Long): Future[List[DBFolder]] = {
    Future.successful {
      DB withSession { implicit session =>
        slickFolders.filter(_.idUser === userId).sortBy(_.left.asc)
          .list
      }
    }
  }

  /**
   * Finds user's root folder
   * @param userId user's id
   * @return a promise of user's root folder (None if user does not have a root folder)
   */
  def findRoot(userId: Long): Future[Option[DBFolder]] = {
    Future.successful {
      DB withSession { implicit session =>
        slickFolders.filter(x => x.idUser === userId && x.left === 0)
          .firstOption
      }
    }
  }

  /**
   * Finds folder by its id
   * @param folderId folder's id
   * @return a promise of a folder with supplied id (None if no folder was found)
   */
  def findById(folderId: Long): Future[Option[DBFolder]] = {
    Future.successful {
      DB withSession { implicit session =>
        slickFolders.filter(_.id === folderId).firstOption
      }
    }
  }

  /**
   * Find folder's immediate children
   * @param folderId parent's folder id
   * @return a promise of a list of immediate children of a folder with supplied id
   */
  def findChildrenById(folderId: Long): Future[List[DBFolder]] = {
    Future.successful {
      DB withSession { implicit session =>
        slickFolders.filter(_.id === folderId).firstOption match {
          case Some(parentFolder) =>
            slickFolders.filter(x =>
              x.left > parentFolder.left
                && x.left > parentFolder.left
                && x.right < parentFolder.right
                && x.level === (parentFolder.level + 1)
            ).list

          case None => Nil
        }

      }
    }
  }

  /**
   * Creates root folder for a user with supplied id
   * @param userId user's id
   * @return a promise of a created folder
   */
  def insertRoot(userId: Long): Future[DBFolder] = {
    Future.successful {
      DB withSession { implicit session =>
        val rootFolder = DBFolder(None, userId, rootFolderName, 0, 0, 1)
        val rootFolderId = (slickFolders returning slickFolders.map(_.id)) += rootFolder
        rootFolder.copy(id = Option(rootFolderId))
      }
    }
  }

  /**
   * Appends sub folder to a folder with supplied id
   * @param parentFolderId parent folder's id
   * @param folderName new folder's name
   * @return a promise of an appended folder (None if parent folder does not exist)
   */
  def appendToFolder(parentFolderId: Long, folderName: String): Future[Option[DBFolder]] = {
    Future.successful {
      DB withSession { implicit session =>
        slickFolders.filter(_.id === parentFolderId).firstOption.map { parentFolder =>
          val parentRight = parentFolder.right

          updateOthers(parentFolder.idUser, parentRight)

          val appendedDBFolder = DBFolder(None, parentFolder.idUser, folderName, parentFolder.level + 1, parentFolder.right, parentFolder.right + 1)
          val appendedDBFolderId = (slickFolders returning slickFolders.map(_.id)) += appendedDBFolder
          appendedDBFolder.copy(id = Option(appendedDBFolderId))
        }
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
