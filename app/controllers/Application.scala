package controllers

import _root_.services.{FolderService, UserService}
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.exceptions.{NotAuthenticatedException, NotAuthorizedException}
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services.AvatarService
import com.mohiva.play.silhouette.api.util.{Credentials, PasswordHasher, PasswordInfo}
import com.mohiva.play.silhouette.impl.authenticators.SessionAuthenticator
import com.mohiva.play.silhouette.impl.exceptions.{IdentityNotFoundException, InvalidPasswordException}
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import daos.silhouette.SilhouetteDAOException
import models.User
import play.api.i18n.MessagesApi
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._
import play.api.mvc._
import scaldi.{Injectable, Injector}

import scala.concurrent.Future

/**
 * Controller handles mostly sign up / sign in routines
 */
class Application(implicit inj: Injector)
  extends Controller with Injectable {

  implicit val messagesApi = inject[MessagesApi]
  implicit val env = inject[Environment[User, SessionAuthenticator]]

  private val userService = inject[UserService]
  private val folderService = inject[FolderService]
  private val authInfoService = inject[AuthInfoRepository]
  private val avatarService = inject[AvatarService]
  private val credentialsProvider = inject[CredentialsProvider]
  private val passwordHasher = inject[PasswordHasher]

  implicit val CredentialsReads: Reads[Credentials] = (
    (JsPath \ "email").read[String](email) and
      (JsPath \ "password").read[String](minLength[String](6))
    )(Credentials.apply _)

  /**
   * Handles the index action.
   * @return index page or cloud index page if user is authenticated
   */
  def index = UserAwareAction { implicit request =>
    request.identity match {
      case Some(user) => Redirect(routes.Cloud.index())
      case None => Ok(views.html.index(""))
    }
  }

  /**
   * Handles the Sign Out action.
   * @return The result to display.
   */
  def signOut = SecuredAction.async { implicit request =>
    val result = Redirect(routes.Application.index())
    env.authenticatorService.discard(request.authenticator, result)
  }

  /**
   * Authenticates a user against the credentials provider.
   * @return redirects to Cloud.index if authentication was successful, otherwise returns error in json format
   */
  def credentialsAuthenticationHandler = Action.async(BodyParsers.parse.json) { implicit request =>
    request.body.validate[Credentials] match {
      case JsSuccess(credentials, _) => authenticateUser(credentials)

      case JsError(errors) => Future.successful(BadRequest(Json.obj(
        "code" -> inject[Int](identified by "errors.auth.loginPasswordNotValid"),
        "fields" -> JsError.toJson(errors)
      )))
    }
  }

  private def authenticateUser(userCredentials: Credentials)(implicit request: Request[_]): Future[Result] = {
    credentialsProvider.authenticate(userCredentials)
      .flatMap(authenticateUserByLoginInfo)
      .recover {
        case e: NotAuthorizedException => InternalServerError(Json.obj(
          "code" -> inject[Int](identified by "errors.auth.accessDenied"),
          "message" -> "Access denied"
        ))
        case e: NotAuthenticatedException => InternalServerError(Json.obj(
          "code" -> inject[Int](identified by "errors.auth.notAuthenticated"),
          "message" -> "Not authenticated"
        ))
        case e: SilhouetteDAOException => InternalServerError(Json.obj(
          "code" -> inject[Int](identified by "errors.auth.userNotFound"),
          "message" -> "User was not found"
        ))
        case e: InvalidPasswordException => InternalServerError(Json.obj(
          "code" -> inject[Int](identified by "errors.auth.userNotFound"),
          "message" -> "User was not found"
        ))
        case e: IdentityNotFoundException => InternalServerError(Json.obj(
          "code" -> inject[Int](identified by "errors.auth.userNotFound"),
          "message" -> "User was not found"
        ))
      }
  }

  private def authenticateUserByLoginInfo(loginInfo: LoginInfo)(implicit request: Request[_]): Future[Result] = {
    userService.retrieve(loginInfo).flatMap {
      case Some(user) => env.authenticatorService.create(user.loginInfo).flatMap { authenticator =>
        val result = Ok(Json.obj(
          "id" -> user.id,
          "email" -> user.email,
          "idRoot" -> user.idRoot
        ))

        env.authenticatorService.init(authenticator).flatMap { value =>
          env.authenticatorService.embed(value, result)
        }
      }
      case None => Future.successful {
        InternalServerError(Json.obj(
          "code" -> inject[Int](identified by "errors.auth.userNotFound"),
          "message" -> "User was not found"
        ))
      }
    }
  }


  /**
   * Registers a new user.
   * @return The result to display.
   */
  def signUpHandler = Action.async(BodyParsers.parse.json) { implicit request =>

    val userCredentials = request.body.validate[Credentials]

    userCredentials match {
      case JsSuccess(credentials, _) =>
        val email = credentials.identifier
        val password = credentials.password
        val loginInfo = LoginInfo(CredentialsProvider.ID, email)
        val hashedPassword = passwordHasher.hash(password)

        userService.retrieve(loginInfo).flatMap {
          case None => createNewUser(loginInfo, email, hashedPassword)

          case Some(_) => Future.successful(InternalServerError(Json.obj(
            "code" -> inject[Int](identified by "errors.auth.userAlreadyExists"),
            "message" -> "User already exists"
          )))
        }

      case JsError(errors) => Future.successful(InternalServerError(Json.obj(
        "code" -> inject[Int](identified by "errors.auth.loginPasswordNotValid"),
        "message" -> "Login or password are not valid",
        "fields" -> JsError.toJson(errors)
      )))
    }
  }

  private def createNewUser(loginInfo: LoginInfo, email: String, password: PasswordInfo)(implicit request: Request[_]) = {
    val result = for {
      avatarURL <- avatarService.retrieveURL(email)
      // new user cannot have root folder's id
      userWithoutIdRoot <- userService.create(loginInfo, email, avatarURL)
      rootFolder <- folderService.createRootForUser(userWithoutIdRoot)
      user <- userService.save(userWithoutIdRoot.copy(idRoot = rootFolder.id))
      _ <- authInfoService.save(loginInfo, password)
      authenticator <- env.authenticatorService.create(user.loginInfo)
      value <- env.authenticatorService.init(authenticator)
      result <- env.authenticatorService.embed(value, Ok(Json.obj(
        "id" -> user.id,
        "email" -> email,
        "idRoot" -> user.idRoot
      )))
    } yield result

    result.recover {
      case e: Exception => InternalServerError(Json.obj(
        "code" -> inject[Int](identified by "errors.application.couldNotCreateNewUser"),
        "message" -> "Could not create new user"
      ))
    }
  }
}
