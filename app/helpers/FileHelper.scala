package helpers

import java.io.File

import scaldi.{Injectable, Injector}


/**
 * Handles movement, retrieving, copying of individual files
 * It's main purpose is mainly to be reinjected in test as something that doesn't move real files
 */
class FileHelper(implicit inj: Injector) extends Injectable {

  protected val applicationPath = inject[play.api.Application].path

  /**
   * Retrieves file from files system in the /uploaded folder
   * User must supply folder id in which the file is stored
   * @param folderId the id of the folder in which file is stored
   * @param identifier book's identifier
   * @param extension book's extension
   * @return java.io.File with book file
   */
  def getUploadedFile(folderId: Long, identifier: String, extension: String): File =
    new java.io.File(s"$applicationPath/upload/$folderId/$identifier.$extension")


}
