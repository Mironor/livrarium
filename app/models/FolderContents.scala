package models

import play.api.libs.functional.syntax._
import play.api.libs.json.{Reads, Writes, _}

case class FolderContents(id: Long,
                          folders: Seq[Folder],
                          books: Seq[Book])

object FolderContents {
  // Implicit conversions for Json Serialisation / Deserialisation
  implicit val folderContentsWrites: Writes[FolderContents] = new Writes[FolderContents]{
    def writes(folderContents: FolderContents) = Json.obj(
      "id" -> folderContents.id,
      "folders" -> folderContents.folders,
      "books" -> folderContents.books
    )
  }

  implicit val folderContentsReads: Reads[FolderContents] = (
    (JsPath \ "id").read[Long] and
      (JsPath \ "folders").read[Seq[Folder]] and
      (JsPath \ "books").read[Seq[Book]]
    )(FolderContents.apply _)
}

