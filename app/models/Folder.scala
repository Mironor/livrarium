package models

import daos.DBTableDefinitions.DBFolder
import play.api.libs.json.{Reads, Writes}
import play.api.libs.json._
import play.api.libs.functional.syntax._

case class Folder(id: Option[Long],
                  name: String,
                  children: List[Folder])

object Folder {
  // Implicit conversions for Json Serialisation / Deserialisation
  implicit val folderWrites: Writes[Folder] = (
    (__ \ "id").write[Option[Long]] and
      (__ \ "label").write[String] and
      (__ \ "children").write[List[Folder]]
    )(unlift(Folder.unapply))

  implicit val folderReads: Reads[Folder] = (
    (__ \ "id").read[Option[Long]] and
      (__ \ "label").read[String] and
      (__ \ "children").read[List[Folder]]
    )(Folder.apply _)

  def fromDBFolder(dbFolder: DBFolder): Folder = Folder(dbFolder.id, dbFolder.name, List())
}


