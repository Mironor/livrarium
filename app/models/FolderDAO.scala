package models

import models.DBTableDefinitions.{DBFolder, Folders}
import scala.slick.jdbc.JdbcBackend.Session
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick._

import scala.concurrent.Future
import scala.slick.lifted.TableQuery

import play.api.Play.current

case class Folder(id: Option[Long],
                  name: String,
                  children: List[Folder])

class FolderDAO {

  val slickFolders = TableQuery[Folders]


  /**
   * Returns root folder with children (recursively)
   * @param user User
   * @return user's folder tree
   */
  def findUserFolderTree(user: User): Future[Option[Folder]] = {
    Future.successful {
      DB withSession { implicit session =>
        val dbRootFolder = slickFolders.filter(folder => folder.idUser === user.id.get && folder.left === 0).firstOption
        dbRootFolder match {
          case Some(rootFolder) => Some(generateFolderTree(rootFolder))
          case None => None
        }
      }
    }
  }

  private def generateFolderTree(folder: DBFolder)(implicit session: Session): Folder = {
    val folderChildrenList = slickFolders.filter(x => x.left > 0 && x.right < folder.right)
      .sortBy(_.left.desc).list

   Folder(folder.id, folder.name, generateFolderChildren(0, folder.right, folderChildrenList))
  }

  private def generateFolderChildren(currentLeft: Int, currentRight: Int, dbFolders: Seq[DBFolder]): List[Folder] = {
    dbFolders match {
      case dbFolder :: tail =>
        if (dbFolder.left == currentRight + 1){
          Nil
        } else if (dbFolder.left > currentLeft){
          Folder(dbFolder.id, dbFolder.name, generateFolderChildren(dbFolder.left, dbFolder.right, tail)) :: generateFolderChildren(dbFolder.right, currentRight, tail)
        } else{
          generateFolderChildren(currentLeft, currentRight, tail)
        }
      case Nil => Nil
    }
  }

  def create(folder: Folder): Folder = {
    Folder(Some(0), "", List())
  }

  def save(rootFolder: Folder) = {
    Future.successful(Folder(Some(0), "", List()))
  }

  def delete(id: String): Unit = {

  }

}
