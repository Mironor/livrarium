package services

import daos.DBTableDefinitions.DBFolder
import daos.FolderDAO
import models.{Folder, User}
import play.api.libs.concurrent.Execution.Implicits._
import scaldi.{Injectable, Injector}

import scala.concurrent.Future
import scala.language.postfixOps

/**
 * There should be no access to FolderDAO except through this class
 * Handles all transformations from DBFolder to Folder (including tree construction)
 */
class FolderService(implicit inj: Injector) extends Injectable {

  val folderDAO = inject[FolderDAO]

  /**
   * User's folder tree starting with root folder
   * Implementation of Nested Sets pattern
   * @param user User
   * @return root folder with recursive children folders (creates root folder if not found)
   */
  def retrieveFolderTree(user: User): Future[Folder] = {
    folderDAO.findAll(user.id).flatMap {
      case rootFolder +: tail => Future.successful {
        Folder.fromDBFolderWithChildren(rootFolder, generateChildren(0, rootFolder.right, tail))
      }
      case _ => createRootForUser(user)
    }
  }

  // this is not tail-rec, so possible overflows on very large folder trees
  private def generateChildren(currentLeft: Int, currentRight: Int, dbFolders: Seq[DBFolder]): Vector[Folder] = {
    dbFolders match {
      case dbFolder +: tail if dbFolder.left > currentRight => Vector.empty[Folder]
      case dbFolder +: tail if dbFolder.left > currentLeft =>
        Folder.fromDBFolderWithChildren(dbFolder, generateChildren(dbFolder.left, dbFolder.right, tail)) +: generateChildren(dbFolder.right, currentRight, tail)
      case dbFolder +: tail => generateChildren(currentLeft, currentRight, tail)
      case _ => Vector.empty[Folder]
    }
  }

  /**
   * Gets root folder for defined user
   * @param user current user
   * @return a promise of user's root folder (None if folder was not found)
   */
  def retrieveRoot(user: User): Future[Option[Folder]] = {
    folderDAO.findRoot(user.id).map {
      _.map(Folder.fromDBFolder)
    }
  }

  /**
   * Retrieves folder by its id
   * @param user current user, folder's owner
   * @param folderId folder's id to retrieve
   * @return a promise of a found folder (None if the folder was not found)
   */
  def retrieve(user: User, folderId: Long): Future[Option[Folder]] = {
    folderDAO.findById(folderId).map {
      _.filter(user.owns).map(Folder.fromDBFolder)
    }
  }

  /**
   * Creates root folder for a supplied user
   * @param user current user
   * @return a promise of a created root folder
   */
  def createRootForUser(user: User): Future[Folder] = {
    folderDAO.insertRoot(user.id)
      .map(Folder.fromDBFolder)
  }


  /**
   * Retrieves folder's children
   * @param user current user, folder's owner
   * @param parentFolderId parent folder's id
   * @return
   */
  def retrieveChildren(user: User, parentFolderId: Long): Future[Seq[Folder]] = {
    folderDAO.findChildrenById(parentFolderId).map {
      _.map(Folder.fromDBFolder)
    }
  }

  /**
   * Appends sub-folder to the root folder
   * @param user current user
   * @param folderName new folder's name
   * @return a promise of appended folder (None if folder was not appended)
   */
  def appendToRoot(user: User, folderName: String): Future[Option[Folder]] = {
    retrieveRoot(user).flatMap {
      case Some(rootFolder) => appendTo(user, rootFolder, folderName)
      case None => Future.successful(None)
    }
  }

  /**
   * Appends sub-folder to supplied folder
   * @param user current user, parent folder's owner
   * @param parentFolder parent folder
   * @param folderName new folder's name
   * @return a promise of appended folder (None if folder was not appended)
   */
  def appendTo(user: User, parentFolder: Folder, folderName: String): Future[Option[Folder]] = {
    appendTo(user, parentFolder.id, folderName)
  }

  /**
   * Appends sub-folder to a folder with supplied folder id
   * @param user current user, parent folder's owner
   * @param parentFolderId parent folder's id
   * @param folderName new folder's name
   * @return a promise of appended folder (None if folder was not appended)
   */
  def appendTo(user: User, parentFolderId: Long, folderName: String): Future[Option[Folder]] = {
    retrieve(user, parentFolderId).flatMap {
      case Some(folder) =>
        folderDAO.appendToFolder(parentFolderId, folderName).map {
          _.map(Folder.fromDBFolder)
        }

      case None => Future.successful(None)
    }
  }
}
