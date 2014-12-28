package services

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
   * Gets root folder for defined user
   * @param user current user
   * @return a promise of user's root folder (None if folder was not found)
   */
  def retrieveRoot(user: User): Future[Option[Folder]] = {
    user.id match {
      case Some(userId) =>
        folderDAO.findRoot(userId).map {
          _.map(Folder.fromDBFolder)
        }

      case None => Future.successful(None)
    }
  }

  /**
   * Retrieves folder by its id
   * @param user current user, folder's owner
   * @param folderId folder's id to retrieve
   * @return a promise of a found folder (None if the folder was not found)
   */
  def retrieveById(user: User, folderId: Long): Future[Option[Folder]] = {
    folderDAO.findById(folderId).map {
      _.filter(user.owns).map(Folder.fromDBFolder)
    }
  }

  /**
   * Creates root folder for a supplied user
   * @param user current user
   * @return a promise of a created root folder (None if the folder was not created)
   */
  def createRootForUser(user: User): Future[Option[Folder]] = {
    user.id match {
      case Some(userId) =>
        folderDAO.insertRoot(userId)
          .map(Folder.fromDBFolder)
          .map(Option.apply)

      case None => Future.successful(None)
    }
  }


  /**
   * Retrieves folder's children
   * @param user current user, folder's owner
   * @param parentFolderId parent folder's id
   * @return
   */
  def retrieveChildren(user: User, parentFolderId: Long): Future[List[Folder]] = {
    //    retrieveById(user, parentFolderId)
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
    parentFolder.id match {
      case Some(folderId) => appendTo(user, folderId, folderName)
      case None => Future.successful(None)
    }
  }

  /**
   * Appends sub-folder to a folder with supplied folder id
   * @param user current user, parent folder's owner
   * @param parentFolderId parent folder's id
   * @param folderName new folder's name
   * @return a promise of appended folder (None if folder was not appended)
   */
  def appendTo(user: User, parentFolderId: Long, folderName: String): Future[Option[Folder]] = {
    retrieveById(user, parentFolderId).flatMap {
      case Some(folder) =>
        folderDAO.appendToFolder(parentFolderId, folderName).map {
          _.map(Folder.fromDBFolder)
        }

      case None => Future.successful(None)
    }
  }
}
