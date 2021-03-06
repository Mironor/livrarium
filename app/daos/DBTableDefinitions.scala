package daos

import slick.driver.PostgresDriver.api._
import slick.lifted.{TableQuery, Tag}
import slick.model.ForeignKeyAction

/**
 * Tables definitions for slick
 */
object DBTableDefinitions {

  case class DBUser(id: Option[Long],
                    idRoot: Long,
                    email: String,
                    avatarURL: Option[String])

  class Users(tag: Tag) extends Table[DBUser](tag, "users") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    
    def idRoot = column[Long]("idRoot")

    def email = column[String]("email")

    def avatarURL = column[Option[String]]("avatarURL")

    def * = (id.?, idRoot, email, avatarURL) <>(DBUser.tupled, DBUser.unapply)
  }

  case class DBLoginInfo(id: Option[Long],
                         idUser: Long,
                         providerID: String,
                         providerKey: String)

  class LoginInfos(tag: Tag) extends Table[DBLoginInfo](tag, "logininfos") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def idUser = column[Long]("idUser")

    def providerID = column[String]("providerID")

    def providerKey = column[String]("providerKey")

    def userFK = foreignKey("LOGININFO_USER_FK", idUser, TableQuery[Users])(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

    def * = (id.?, idUser, providerID, providerKey) <>(DBLoginInfo.tupled, DBLoginInfo.unapply)
  }

  case class DBOAuth1Info(idLoginInfo: Long,
                          token: String,
                          secret: String)

  class OAuth1Infos(tag: Tag) extends Table[DBOAuth1Info](tag, "oauth1infos") {
    def idLoginInfo = column[Long]("idLoginInfo", O.PrimaryKey)

    def token = column[String]("token")

    def secret = column[String]("secret")

    def loginInfoFK = foreignKey("OAUTH1INFO_LOGININFO_FK", idLoginInfo, TableQuery[LoginInfos])(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

    def * = (idLoginInfo, token, secret) <>(DBOAuth1Info.tupled, DBOAuth1Info.unapply)
  }

  case class DBOAuth2Info(idLoginInfo: Long,
                          accessToken: String,
                          tokenType: Option[String],
                          expiresIn: Option[Int],
                          refreshToken: Option[String])

  class OAuth2Infos(tag: Tag) extends Table[DBOAuth2Info](tag, "oauth2infos") {
    def idLoginInfo = column[Long]("idLoginInfo", O.PrimaryKey)

    def accessToken = column[String]("accesstoken")

    def tokenType = column[Option[String]]("tokentype")

    def expiresIn = column[Option[Int]]("expiresin")

    def refreshToken = column[Option[String]]("refreshtoken")

    def loginInfoFK = foreignKey("OAUTH2INFO_LOGININFO_FK", idLoginInfo, TableQuery[LoginInfos])(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

    def * = (idLoginInfo, accessToken, tokenType, expiresIn, refreshToken) <>(DBOAuth2Info.tupled, DBOAuth2Info.unapply)
  }

  case class DBPasswordInfo(idLoginInfo: Long,
                            hasher: String,
                            password: String,
                            salt: Option[String])

  class PasswordInfos(tag: Tag) extends Table[DBPasswordInfo](tag, "passwordinfos") {
    def idLoginInfo = column[Long]("idLoginInfo", O.PrimaryKey)

    def hasher = column[String]("hasher")

    def password = column[String]("password")

    def salt = column[Option[String]]("salt")

    def loginInfoFK = foreignKey("PASSWORDINFO_LOGININFO_FK", idLoginInfo, TableQuery[LoginInfos])(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

    def * = (idLoginInfo, hasher, password, salt) <>(DBPasswordInfo.tupled, DBPasswordInfo.unapply)
  }

  case class DBBook(id: Option[Long],
                    idUser: Long,
                    uuid: String,
                    name: String,
                    format: String,
                    totalPages: Int = 0,
                    currentPage: Int = 1)

  /**
   * Many-to-one with user
   * Many-to-many with folders
   */
  class Books(tag: Tag) extends Table[DBBook](tag, "books") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def idUser = column[Long]("idUser")

    def uuid = column[String]("uuid")

    def name = column[String]("name")

    def format = column[String]("format")

    def pages = column[Int]("pages")

    def currentPage = column[Int]("currentPage")

    def userFK = foreignKey("BOOK_USER_FK", idUser, TableQuery[Users])(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

    def * = (id.?, idUser, uuid, name, format, pages, currentPage) <>(DBBook.tupled, DBBook.unapply)

    def uniqueUUID = index("UNIQUE_UUID", uuid, unique = true)
  }

  case class BookToFolder(idBook: Long,
                          idFolder: Long)

  class BooksToFolders(tag: Tag) extends Table[BookToFolder](tag, "books_to_folders") {
    def idBook = column[Long]("idBook")

    def idFolder = column[Long]("idFolder")

    def pk = primaryKey("pk_books_to_folders", (idBook, idFolder))

    def bookFK = foreignKey("BOOK_BOOKS_TO_FOLDERS_FK", idBook, TableQuery[Books])(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

    def folderFK = foreignKey("FOLDER_BOOKS_TO_FOLDERS_FK", idFolder, TableQuery[Folders])(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

    def * = (idBook, idFolder) <>(BookToFolder.tupled, BookToFolder.unapply)
  }

  case class DBFolder(id: Option[Long],
                      idUser: Long,
                      name: String,
                      level: Int,
                      left: Int,
                      right: Int)

  class Folders(tag: Tag) extends Table[DBFolder](tag, "folders") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def idUser = column[Long]("idUser")

    def name = column[String]("name")

    def level = column[Int]("level")

    def left = column[Int]("left")

    def right = column[Int]("right")

    def userFK = foreignKey("FOLDER_USER_FK", idUser, TableQuery[Users])(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

    def * = (id.?, idUser, name, level, left, right) <>(DBFolder.tupled, DBFolder.unapply)
  }

}
