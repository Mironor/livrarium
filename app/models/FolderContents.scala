package models

import play.api.libs.functional.syntax._
import play.api.libs.json.{Reads, Writes, _}

case class FolderContents(id: Long,
                          folders: Seq[Folder],
                          books: Seq[Book])

object FolderContents {
  // Implicit conversions for Json Serialisation / Deserialisation
  implicit val folderContentsWrites: Writes[FolderContents] = (
    (__ \ "id").write[Long] and
      (__ \ "folders").write[Seq[Folder]] and
      (__ \ "books").write[Seq[Book]]
    )(unlift(FolderContents.unapply))

  implicit val folderContentsReads: Reads[FolderContents] = (
    (__ \ "id").read[Long] and
      (__ \ "folders").read[Seq[Folder]] and
      (__ \ "books").read[Seq[Book]]
    )(FolderContents.apply _)
}

