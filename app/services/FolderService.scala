package services

import models.DBTableDefinitions.DBFolder
import models.FolderDAO
import scaldi.{Injectable, Injector}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

case class Folder(id: Option[Long],
                  name: String,
                  children: List[Folder])

/**
 * Handles all transformations from DBFolder to Folder (including tree construction)
 */
class FolderService(implicit inj: Injector) extends Injectable {

  val folderDAO = inject[FolderDAO]

  /**
   * Root folder should never be exposed to api. Thus we return its immediate children with their children recursively
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
      case dbFolder :: tail if dbFolder.left == currentRight + 1 => Nil
      case dbFolder :: tail if dbFolder.left > currentLeft => Folder(dbFolder.id, dbFolder.name, generateChildren(dbFolder.left, dbFolder.right, tail)) :: generateChildren(dbFolder.right, currentRight, tail)
      case dbFolder :: tail => generateChildren(currentLeft, currentRight, tail)
      case Nil => Nil
    }
  }

  def createRootForUser(user: User): Future[_] = folderDAO.createRootForUser(user)

  def appendToRoot(user: User, folderName: String): Future[Folder] = {
    val dbRootFolderPromise = folderDAO.getUserRoot(user)
    val appendedDBFolderPromise = appendToPromise(user, dbRootFolderPromise, folderName)
    appendedDBFolderPromise.map(dbFolder => Folder(dbFolder.id, dbFolder.name, Nil))
  }

  def appendTo(user: User, parentFolder: Folder, folderName: String): Future[Folder] ={
    val dbParentFolderPromise = folderDAO.getById(user, parentFolder.id.get)
    val appendedDBFolderPromise = appendToPromise(user, dbParentFolderPromise, folderName)
    appendedDBFolderPromise.map(dbFolder => Folder(dbFolder.id, dbFolder.name, Nil))
  }

  private def appendToPromise(user: User, parentFolderPromise: Future[Option[DBFolder]], folderName: String): Future[DBFolder] = {
    parentFolderPromise.flatMap {
      _.map {
        parentFolder: DBFolder => folderDAO.appendToFolder(user, parentFolder, folderName)
      } getOrElse {
        throw FolderNotFoundException("Folder is not defined for userId=" + user.id)
      }
    }
  }

}

case class FolderNotFoundException(message: String) extends Exception(message)