package services

import models.DBTableDefinitions.DBFolder
import models.FolderDAO
import play.api.libs.concurrent.Execution.Implicits._
import scaldi.{Injectable, Injector}

import scala.concurrent.Future

case class Folder(id: Option[Long],
                  name: String,
                  children: List[Folder])

/**
 * There should be no access to FolderDAO except through this class
 * Handles all transformations from DBFolder to Folder (including tree construction)
 */
class FolderService(implicit inj: Injector) extends Injectable {

  val folderDAO = inject[FolderDAO]

  /**
   * Root folder should never be exposed to api. Thus we return its immediate children with their children recursively
   * Implementation of Nested Sets pattern
   * @param user User
   * @return list of immediate children of root's folder.
   */
  def retrieveUserFolderTree(user: User): Future[List[Folder]] = {
    val dbFoldersPromise = folderDAO.findUserFolders(user)
    dbFoldersPromise.map {
      case rootFolder :: tail => generateChildren(0, rootFolder.right, tail)
      case Nil => Nil
    }
  }

  private def generateChildren(currentLeft: Int, currentRight: Int, dbFolders: Seq[DBFolder]): List[Folder] = {
    dbFolders match {
      case dbFolder :: tail if dbFolder.left > currentRight => Nil
      case dbFolder :: tail if dbFolder.left > currentLeft => Folder(dbFolder.id, dbFolder.name, generateChildren(dbFolder.left, dbFolder.right, tail)) :: generateChildren(dbFolder.right, currentRight, tail)
      case dbFolder :: tail => generateChildren(currentLeft, currentRight, tail)
      case Nil => Nil
    }
  }

  /**
   * Creates root folder for supplied user
   * @param user User
   * @return
   */
  def createRootForUser(user: User): Future[_] = folderDAO.createRootForUser(user)

  /**
   * Gets root folder for defined user
   * @param user User
   * @return
   */
  def retrieveRoot(user: User): Future[Option[Folder]] = {
    folderDAO.findUserRoot(user).map { dbFolderOption =>
      dbFolderOption.map {
        dbFolder => Folder(dbFolder.id, dbFolder.name, List())
      }
    }
  }

  /**
   * Gets folder's children folders
   * @param user User
   * @param folderId parent folder's id
   * @return
   */
  def retrieveChildren(user: User, folderId: Long): Future[List[Folder]] = {
    folderDAO.findChildren(user, folderId).map{
      _.map(dbFolder => Folder(dbFolder.id, dbFolder.name, List()))
    }
  }

  /**
   * Appends sub-folder with a given name to the root folder
   * @param user User
   * @param folderName new folder's label
   * @return
   */
  def appendToRoot(user: User, folderName: String): Future[Folder] = {
    val dbRootFolderPromise = folderDAO.findUserRoot(user)
    val appendedDBFolderPromise = appendToPromise(user, dbRootFolderPromise, folderName)
    appendedDBFolderPromise.map(dbFolder => Folder(dbFolder.id, dbFolder.name, Nil))
  }

  /**
   * Appends sub-folder to a supplied user
   * @param user User
   * @param parentFolderId parent folder id
   * @param folderName new folder's label
   * @return
   */
  def appendTo(user: User, parentFolderId: Long, folderName: String): Future[Folder] = {
    val dbParentFolderPromise = folderDAO.findById(user, parentFolderId)
    val appendedDBFolderPromise = appendToPromise(user, dbParentFolderPromise, folderName)
    appendedDBFolderPromise.map(dbFolder => Folder(dbFolder.id, dbFolder.name, Nil))
  }

  def appendTo(user: User, parentFolder: Folder, folderName: String): Future[Folder] = {
    val parentFolderId = parentFolder.id.getOrElse(throw FolderNotFoundException("Folder should have id"))
    appendTo(user, parentFolderId, folderName)
  }

  private def appendToPromise(user: User, parentFolderPromise: Future[Option[DBFolder]], folderName: String): Future[DBFolder] = {
    parentFolderPromise.flatMap { parentFolderOption =>
      val parentFolder = parentFolderOption.getOrElse(throw FolderNotFoundException("Folder is not defined for userId=" + user.id))
      folderDAO.appendToFolder(user, parentFolder, folderName)
    }
  }

}

case class FolderNotFoundException(message: String) extends Exception(message)