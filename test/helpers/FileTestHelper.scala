package helpers

import java.io.File

import scaldi.Injector


class FileTestHelper(implicit inj: Injector) extends FileHelper{

  override def getUploadedFile(folderId: Long, identifier: String, extension: String): File =
    new java.io.File(s"$applicationPath/test/resources/$identifier.$extension")

}
