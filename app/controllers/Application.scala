package controllers

import _root_.services.{FolderService, UserService}
import com.mohiva.play.silhouette.api.{Silhouette, _}
import com.mohiva.play.silhouette.api.exceptions.{NotAuthorizedException, NotAuthenticatedException}
import com.mohiva.play.silhouette.api.services.{AuthInfoService, AvatarService}
import com.mohiva.play.silhouette.api.util.{Credentials, PasswordHasher, PasswordInfo}
import com.mohiva.play.silhouette.impl.authenticators.SessionAuthenticator
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import models.User
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
  extends Silhouette[User, SessionAuthenticator] with Injectable {

  implicit val env = inject[Environment[User, SessionAuthenticator]]

  val userService = inject[UserService]
  val folderService = inject[FolderService]
  val authInfoService = inject[AuthInfoService]
  val avatarService = inject[AvatarService]
  val passwordHasher = inject[PasswordHasher]

  implicit val CredentialsReads: Reads[Credentials] = (
    (JsPath \ "email").read[String](email) and
      (JsPath \ "password").read[String](minLength[String](6))
    )(Credentials.apply _)

  /**
   * Handles the index action.
   *
   * @return index page or cloud index page if user is authenticated
   */
  def index = UserAwareAction { implicit request =>
    request.identity match {
      case Some(user) => Redirect(routes.Cloud.index())
      case None => Ok(views.html.index())
    }
  }

  /**
   * Handles the signUp action.
   * Same as index, the client-side router will handle the sing up form rendering
   *
   * @return index page or cloud index page if user is authenticated
   */
  def signUp = index

  /**
   * Handles the Sign Out action.
   *
   * @return The result to display.
   */
  def signOut = SecuredAction { implicit request =>
    val result = Redirect(routes.Application.index())
    request.authenticator.discard(result)
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
        "fields" -> JsError.toFlatJson(errors)
      )))
    }
  }

  def authenticateUser(userCredentials: Credentials)(implicit request: Request[_]): Future[Result] = {
    credentialsAuthentication(userCredentials).flatMap { loginInfo =>
      userService.retrieve(loginInfo).flatMap {
        case Some(user) => env.authenticatorService.create(user.loginInfo).flatMap { authenticator =>
          val result = Future.successful(
            Ok(Json.obj("email" -> user.email))
          )

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
    }.recover {
      case e: NotAuthorizedException => InternalServerError(Json.obj(
        "code" -> inject[Int](identified by "errors.auth.accessDenied"),
        "message" -> "Access denied"
      ))
      case e: NotAuthenticatedException => InternalServerError(Json.obj(
        "code" -> inject[Int](identified by "errors.auth.notAuthenticated"),
        "message" -> "Not authenticated"
      ))
    }
  }

  private def credentialsAuthentication(userCredentials: Credentials) = env.providers.get(CredentialsProvider.ID) match {
    case Some(credentialsProvider: CredentialsProvider) => credentialsProvider.authenticate(userCredentials)
    case _ => Future.failed(new NotAuthenticatedException(s"Cannot find credentials provider"))
  }


  /**
   * Registers a new user.
   *
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
        "fields" -> JsError.toFlatJson(errors)
      )))
    }
  }

  private def createNewUser(loginInfo: LoginInfo, email: String, password: PasswordInfo)(implicit request: Request[_]) = {

    val result = for {
      avatarURL <- avatarService.retrieveURL(email)
      user <- userService.create(loginInfo, email, avatarURL)
      _ <- folderService.createRootForUser(user)
      _ <- authInfoService.save(loginInfo, password)
      authenticator <- env.authenticatorService.create(user.loginInfo)
      value <- env.authenticatorService.init(authenticator)
      result <- env.authenticatorService.embed(value, Future.successful {
        Ok(Json.obj("email" -> email))
      })
    } yield result

    result.recover {
      case e: Exception => InternalServerError(Json.obj(
        "code" -> inject[Int](identified by "errors.application.couldNotCreateNewUser"),
        "message" -> "Could not create new user"
      ))
    }
  }
}
