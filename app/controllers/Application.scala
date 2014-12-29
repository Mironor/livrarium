package controllers

import _root_.services.{FolderService, UserService}
import com.mohiva.play.silhouette.api.{Silhouette, _}
import com.mohiva.play.silhouette.api.exceptions.{AccessDeniedException, AuthenticationException}
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
  def index = UserAwareAction.async { implicit request =>
    request.identity match {
      case Some(user) => Future.successful(Redirect(routes.Cloud.index()))
      case None => Future.successful(Ok(views.html.index()))
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
  def signOut = SecuredAction.async { implicit request =>
    val result = Future.successful(Redirect(routes.Application.index()))
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
          val result = Future.successful(Ok(Json.obj("identity" -> user.email)))
          env.authenticatorService.init(authenticator, result)
        }
        case None => Future.successful {
          BadRequest(Json.obj(
            "code" -> inject[Int](identified by "errors.auth.userNotFound")
          ))
        }
      }
    } recover {
      case e: AccessDeniedException => BadRequest(Json.obj(
        "code" -> inject[Int](identified by "errors.auth.accessDenied")
      ))
      case e: AuthenticationException => BadRequest(Json.obj(
        "code" -> inject[Int](identified by "errors.auth.notAuthenticated")
      ))
    }
  }

  private def credentialsAuthentication(userCredentials: Credentials) = env.providers.get(CredentialsProvider.ID) match {
    case Some(credentialsProvider: CredentialsProvider) => credentialsProvider.authenticate(userCredentials)
    case _ => Future.failed(new AuthenticationException(s"Cannot find credentials provider"))
  }


  /**
   * Registers a new user.
   *
   * @return The result to display.
   */
  def signUpHandler = Action.async(BodyParsers.parse.json) { implicit request =>

    val userCredentials = request.body.validate[Credentials]

    userCredentials match {
      case credentials: JsSuccess[Credentials] =>
        val email = credentials.get.identifier
        val password = credentials.get.password
        val loginInfo = LoginInfo(CredentialsProvider.ID, email)
        val hashedPassword = passwordHasher.hash(password)

        userService.retrieve(loginInfo).flatMap {
          case Some(_) => Future.successful(BadRequest(Json.obj(
            "code" -> inject[Int](identified by "errors.auth.userAlreadyExists")
          )))

          case None => createNewUser(loginInfo, email, hashedPassword)
        }

      case errors: JsError => Future.successful(BadRequest(Json.obj(
        "code" -> inject[Int](identified by "errors.auth.loginPasswordNotValid"),
        "fields" -> JsError.toFlatJson(errors)
      )))
    }
  }

  def createNewUser(loginInfo: LoginInfo, email: String, password: PasswordInfo)(implicit request: Request[_]) = {

    val result = for {
      avatarURL <- avatarService.retrieveURL(email)
      userOption <- userService.save(User(None, loginInfo, Some(email), avatarURL))

      // User creation may go bad
      user = userOption.getOrElse(throw new Exception("User was not created"))

      _ <- folderService.createRootForUser(user)
      _ <- authInfoService.save(loginInfo, password)
      authenticator <- env.authenticatorService.create(user.loginInfo)
      result <- env.authenticatorService.init(authenticator, Future.successful {
        Ok(Json.obj("identity" -> email))
      })
    } yield result

    result.recover {
      case e: Exception => BadRequest(Json.obj(
        "code" -> inject[Int](identified by "errors.application.couldNotCreateNewUser"),
        "message" -> "Could not create new user"
      ))
    }
  }
}
