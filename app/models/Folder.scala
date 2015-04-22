package models

import daos.DBTableDefinitions.DBFolder
import play.api.libs.json.{Reads, Writes}
import play.api.libs.json._
import play.api.libs.functional.syntax._

case class Folder(id: Long,
                  name: String,
                  children: Seq[Folder] = Nil)

object Folder {
  // Implicit conversions for Json Serialisation / Deserialisation
  implicit val folderWrites: Writes[Folder] = (
    (__ \ "id").write[Long] and
      (__ \ "name").write[String] and
      (__ \ "children").lazyWrite(Writes.seq(folderWrites))
    )(unlift(Folder.unapply))

  implicit val folderReads: Reads[Folder] = (
    (__ \ "id").read[Long] and
      (__ \ "name").read[String] and
      (__ \ "children").lazyRead(Reads.seq[Folder](folderReads))
    )(Folder.apply _)

  /**
   * Casts dbFolder into Folder with empty children
   * @param dbFolder db row with folder info
   * @return
   */
  def fromDBFolder(dbFolder: DBFolder): Folder = {
    val id = dbFolder.id.getOrElse(throw new Exception(
      """A folder row in the database did not have
        | an id (id field has autoincrement constraint, so it should not be null).
        | Or you are trying to cast a user's row that does not have id (this is strange)""".stripMargin
    ))

    Folder(id, dbFolder.name, Nil)
  }

  /**
   * Casts dbFolder into Folder with children supplied in parameters
   * @param dbFolder db row with folder info
   * @param children folder's children
   * @return
   */
  def fromDBFolderWithChildren(dbFolder: DBFolder, children: List[Folder]) = {
    val folder = fromDBFolder(dbFolder)
    folder.copy(children = children)
  }
}


