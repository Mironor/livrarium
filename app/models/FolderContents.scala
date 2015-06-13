package models

import play.api.libs.functional.syntax._
import play.api.libs.json.{Reads, Writes, _}

case class FolderContents(id: Long,
                          folders: Seq[Folder])

object FolderContents{
  // Implicit conversions for Json Serialisation / Deserialisation
  implicit val folderContentsWrites: Writes[FolderContents] = (
    (__ \ "id").write[Long] and
      (__ \ "folders").write[Seq[Folder]]
    )(unlift(FolderContents.unapply))

  implicit val folderContentsReads: Reads[FolderContents] = (
    (__ \ "id").read[Long] and
      (__ \ "folders").read[Seq[Folder]]
    )(FolderContents.apply _)
}

