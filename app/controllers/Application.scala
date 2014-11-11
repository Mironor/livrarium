package controllers

import _root_.services.{FolderService, User, UserService}
import com.mohiva.play.silhouette.api.Silhouette
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.exceptions.{AccessDeniedException, AuthenticationException}
import com.mohiva.play.silhouette.api.services.{AuthInfoService, AvatarService}
import com.mohiva.play.silhouette.impl.authenticators.SessionAuthenticator
import com.mohiva.play.silhouette.impl.providers.{PasswordInfo, CredentialsProvider, Credentials, PasswordHasher}
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._
import play.api.mvc._
import scaldi.{Injectable, Injector}

import scala.concurrent.Future

/**
 * The basic application controller.
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
   * @return The result to display.
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
   * @return The result to display.
   */
  def signUp = index

  /**
   * Handles the Sign Out action.
   *
   * @return The result to display.
   */
  def signOut = SecuredAction.async { implicit request =>
    val result = Future.successful(Redirect(routes.Application.index()))
    env.eventBus.publish(LogoutEvent(request.identity, request, request2lang))

    request.authenticator.discard(result)
  }

  /**
   * Authenticates a user against the credentials provider.
   *
   * @return The result to display.
   */
  def credentialsAuthenticationHandler = Action.async(BodyParsers.parse.json) { implicit request =>

    val userCredentials = request.body.validate[Credentials]

    userCredentials match {
      case credentials: JsSuccess[Credentials] => authenticateUser(credentials.get)
      case errors: JsError => Future.successful(BadRequest(Json.obj(
        "code" -> inject[Int](identified by "errors.auth.loginPasswordNotValid"),
        "fields" -> JsError.toFlatJson(errors)
      )))
    }
  }

  def authenticateUser(userCredentials: Credentials)(implicit request: Request[_]) = {
    credentialsAuthentication(userCredentials).flatMap { loginInfo =>
      userService.retrieve(loginInfo).flatMap {
        case Some(user) => env.authenticatorService.create(user.loginInfo).flatMap { authenticator =>
          val result = Future.successful(Ok(Json.obj("identity" -> user.email)))
          env.eventBus.publish(LoginEvent(user, request, request2lang))
          env.authenticatorService.init(authenticator, result)
        }
        case None => Future.successful {
          BadRequest(Json.obj(
            "code" -> inject[Int](identified by "errors.auth.noAuthenticator")
          ))
        }
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
        val authInfo = passwordHasher.hash(password)

        userService.retrieve(loginInfo).flatMap {
          case Some(_) => Future.successful(BadRequest(Json.obj(
            "code" -> inject[Int](identified by "errors.auth.userAlreadyExists")
          )))

          case None => createNewUser(loginInfo, email, authInfo)
        }

      case errors: JsError => Future.successful(BadRequest(Json.obj(
        "code" -> inject[Int](identified by "errors.auth.loginPasswordNotValid"),
        "fields" -> JsError.toFlatJson(errors)
      )))
    }
  }

  def createNewUser(loginInfo: LoginInfo, email: String, password: PasswordInfo)(implicit request: Request[_]) = {
    val user = User(
      id = None,
      loginInfo = loginInfo,
      email = Some(email),
      avatarURL = None
    )

    val userPromise = for {
      avatar <- avatarService.retrieveURL(email)
      user <- userService.saveWithLoginInfo(user.copy(avatarURL = avatar))
    } yield user

    userPromise.flatMap { user =>
      for {
        folders <- folderService.createRootForUser(user)
        authInfo <- authInfoService.save(loginInfo, password)
        authenticator <- env.authenticatorService.create(user.loginInfo)
        result <- env.authenticatorService.init(authenticator, Future.successful {
          Ok(Json.obj("identity" -> user.email))
        })
      } yield {
        env.eventBus.publish(SignUpEvent(user, request, request2lang))
        env.eventBus.publish(LoginEvent(user, request, request2lang))
        result
      }
    }
  }
}
