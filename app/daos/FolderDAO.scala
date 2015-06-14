package daos

import daos.DBTableDefinitions.{DBFolder, Folders}

import scaldi.{Injector, Injectable}
import slick.driver.JdbcProfile
import play.api.db.slick._
import slick.driver.PostgresDriver.api._
import slick.lifted.TableQuery

import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future

class FolderDAO(implicit inj: Injector) extends HasDatabaseConfig[JdbcProfile] with Injectable {
  val dbConfig = inject[DatabaseConfigProvider].get[JdbcProfile]


  val rootFolderName = ""
  val slickFolders = TableQuery[Folders]

  /**
   * Gets all user's folders ordered by `left` field (see Nested Sets pattern)
   * @param userId current user's id
   * @return a promise of a list of user's folders ordered by `left` field
   */
  def findAll(userId: Long): Future[Seq[DBFolder]] = db.run {
    slickFolders.filter(_.idUser === userId).sortBy(_.left.asc).result
  }

  /**
   * Finds user's root folder
   * @param userId user's id
   * @return a promise of user's root folder (None if user does not have a root folder)
   */
  def findRoot(userId: Long): Future[Option[DBFolder]] = db.run {
    slickFolders.filter(x => x.idUser === userId && x.left === 0).result.headOption
  }

  /**
   * Finds folder by its id
   * @param folderId folder's id
   * @return a promise of a folder with supplied id (None if no folder was found)
   */
  def findById(folderId: Long): Future[Option[DBFolder]] = db.run {
    slickFolders.filter(_.id === folderId).result.headOption
  }

  /**
   * Find folder's immediate children
   * @param folderId parent's folder id
   * @return a promise of a list of immediate children of a folder with supplied id
   */
  def findChildrenById(folderId: Long): Future[Seq[DBFolder]] = db.run {
    slickFolders.filter(_.id === folderId).result.headOption
  }.flatMap {
    case Some(parentFolder) => db.run {
      slickFolders.filter(x =>
        x.left > parentFolder.left
          && x.left > parentFolder.left
          && x.right < parentFolder.right
          && x.level === (parentFolder.level + 1)
      ).result
    }
    case None => Future.successful(Nil)
  }


  /**
   * Creates root folder for a user with supplied id
   * @param userId user's id
   * @return a promise of a created folder
   */
  def insertRoot(userId: Long): Future[DBFolder] = db.run {
    (slickFolders returning slickFolders.map(_.id)) += DBFolder(None, userId, rootFolderName, 0, 0, 1)
  }.map(id => DBFolder(Some(id), userId, rootFolderName, 0, 0, 1))

  /**
   * Appends sub folder to a folder with supplied id
   * @param parentFolderId parent folder's id
   * @param folderName new folder's name
   * @return a promise of an appended folder (None if parent folder does not exist)
   */
  def appendToFolder(parentFolderId: Long, folderName: String): Future[Option[DBFolder]] = db.run {
    slickFolders.filter(_.id === parentFolderId).result.headOption
  }.flatMap {
    case Some(parentFolder) =>
      try {
        val parentRight = parentFolder.right
        val userId = parentFolder.idUser
        db.run(for {
          _ <- sqlu"""UPDATE "folders" SET "right" = "right" + 2 where "idUser" = $userId AND "right" >= $parentRight"""
          _ <- sqlu"""UPDATE "folders" SET "left" = "left" + 2 where "idUser" = $userId AND "left" > $parentRight"""
          id <- (slickFolders returning slickFolders.map(_.id)) += DBFolder(None, parentFolder.idUser, folderName, parentFolder.level + 1, parentFolder.right, parentFolder.right + 1)
        } yield Some(DBFolder(Option(id), parentFolder.idUser, folderName, parentFolder.level + 1, parentFolder.right, parentFolder.right + 1)))
      } catch {
        case e: Exception => throw new DAOException("Append folder: slick could not execute plain query")
      }

    case None => Future.successful(None)
  }
}
