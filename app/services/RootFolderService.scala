package services

import com.mohiva.play.silhouette.core.LoginInfo
import com.mohiva.play.silhouette.core.providers.CommonSocialProfile
import com.mohiva.play.silhouette.core.services.{AuthInfo, IdentityService}
import models._
import org.bson.types.ObjectId
import play.api.libs.concurrent.Execution.Implicits._
import scaldi.{Injectable, Injector}

import scala.concurrent.Future

/**
 * Handles actions to users.
 *
 */
class RootFolderService(implicit inj: Injector) extends Injectable {
  val rootFolderDAO  = inject[RootFolderDAO]

  def retrieve(user: User) = rootFolderDAO.findByUser(user)

  def save(folder: RootFolder) = rootFolderDAO.save(folder)

  /**
   * Saves the root folder for a user
   *
   * If the root folder exists for this user then update the root folder, otherwise create a new one for the user
   *
   * @param folders The root folder to be updated
   * @param user The user to which the root folder is attached
   * @return the created root folder
   */
  def save(folders: List[Folder], user: User): Future[RootFolder] = {
    rootFolderDAO.findByUser(user) match {
      case Some(rootFolder) =>
        rootFolderDAO.save(rootFolder.copy(
          children = folders
        ))
      case None =>
        rootFolderDAO.save(RootFolder(
          userId = user.id,
          children = folders
        ))
    }
  }
}
