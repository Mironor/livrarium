package services

import models.DBTableDefinitions.DBFolder
import models.{User, FolderDAO}
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
    val dbRootFolderPromise = folderDAO.findUserFolders(user)

    dbRootFolderPromise.map {
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
}
