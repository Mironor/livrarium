package helpers

import com.mohiva.play.silhouette.api.Environment
import com.mohiva.play.silhouette.impl.authenticators.SessionAuthenticator
import daos.DBTableDefinitions._
import models.User
import modules.SpecModule
import org.specs2.execute.AsResult
import play.api.test.PlaySpecification
import scaldi.play.ScaldiApplicationBuilder
import scaldi.play.ScaldiApplicationBuilder._
import scaldi.{Injectable, Injector}
import slick.driver.PostgresDriver.api._

abstract class LivrariumSpecification extends PlaySpecification with Injectable {

  private val application = new ScaldiApplicationBuilder().prependModule(new SpecModule)

  implicit lazy val injector: Injector = application.buildInj()

  implicit lazy val silhouetteEnv: Environment[User, SessionAuthenticator] = inject[Environment[User, SessionAuthenticator]]

  lazy val database = Database.forConfig("slick.dbs.default.db")

  val slickUsers = TableQuery[Users]
  val slickLoginInfos = TableQuery[LoginInfos]
  val slickOAuth1Infos = TableQuery[OAuth1Infos]
  val slickOAuth2Infos = TableQuery[OAuth2Infos]
  val slickPasswordInfos = TableQuery[PasswordInfos]
  val slickBooks = TableQuery[Books]
  val slickFolders = TableQuery[Folders]

  /**
   * This automatically handles up and down evolutions at the beginning and at the end of a spec respectively
   */
  def around[T: AsResult](t: => T) = {
    withScaldiApp() {
      bootstrapFixtures()
      val out = AsResult(t)
      /*
      Await.result(database.run {
        sqlu"""
        alter table "passwordinfos" drop constraint "PASSWORDINFO_LOGININFO_FK";
        alter table "oauth2infos" drop constraint "OAUTH2INFO_LOGININFO_FK";
        alter table "oauth1infos" drop constraint "OAUTH1INFO_LOGININFO_FK";
        alter table "logininfos" drop constraint "LOGININFO_USER_FK";
        alter table "folders" drop constraint "FOLDER_USER_FK";
        alter table "books_to_folders" drop constraint "BOOK_BOOKS_TO_FOLDERS_FK";
        alter table "books_to_folders" drop constraint "FOLDER_BOOKS_TO_FOLDERS_FK";
        alter table "books" drop constraint "BOOK_USER_FK";
        drop table "users";
        drop table "passwordinfos";
        drop table "oauth2infos";
        drop table "oauth1infos";
        drop table "logininfos";
        drop table "folders";
        alter table "books_to_folders" drop constraint "pk_books_to_folders";
        drop table "books_to_folders";
        drop table "books";"""
      }, 1 second)
      */
      out
    }
  }

  protected def bootstrapFixtures(): Unit
}
