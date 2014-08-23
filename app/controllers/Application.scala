package controllers

import _root_.services.{RootFolderService, UserService}
import com.mohiva.play.silhouette.contrib.services.CachedCookieAuthenticator
import com.mohiva.play.silhouette.core._
import com.mohiva.play.silhouette.core.exceptions.{AccessDeniedException, AuthenticationException}
import com.mohiva.play.silhouette.core.providers._
import com.mohiva.play.silhouette.core.services.{AuthInfoService, AvatarService}
import com.mohiva.play.silhouette.core.utils.PasswordHasher
import models.{Folder, RootFolder, RootFolderDAO, User}
import org.bson.types.ObjectId
import play.api.libs.concurrent.Execution.Implicits._

import play.api.libs.json.Reads._
import play.api.libs.json._
import play.api.libs.functional.syntax._

import play.api.mvc.{Action, BodyParsers}
import scaldi.{Injectable, Injector}

import scala.concurrent.Future

/**
 * The basic application controller.
 */
class Application(implicit inj: Injector)
  extends Silhouette[User, CachedCookieAuthenticator] with Injectable {

  implicit val env = inject[Environment[User, CachedCookieAuthenticator]]

  val userService = inject[UserService]
  val rootFolderService = inject[RootFolderService]
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
    env.eventBus.publish(LogoutEvent(request.identity, request, request2lang))
    Future.successful(env.authenticatorService.discard(Redirect(routes.Application.index())))
  }

  /**
   * Authenticates a user against the credentials provider.
   *
   * @return The result to display.
   */
  def credentialsAuthenticationHandler = Action.async(BodyParsers.parse.json) { implicit request =>

    val userCredentials = request.body.validate[Credentials]

    userCredentials match {
      case credentials: JsSuccess[Credentials] => credentialsAuthentication(credentials.get).flatMap { loginInfo =>
        userService.retrieve(loginInfo).flatMap {
          case Some(user) => env.authenticatorService.create(user).map {
            case Some(authenticator) =>
              env.eventBus.publish(LoginEvent(user, request, request2lang))
              env.authenticatorService.send(authenticator, Ok(Json.obj("identity" -> user.email)))
            case None => BadRequest(Json.obj(
              "code" -> inject[Int](identified by "errors.auth.noAuthenticator")
            ))
          }
          case None => Future.failed(new AuthenticationException(""))
        }
      } recover {
        case e: AccessDeniedException => BadRequest(Json.obj(
          "code" -> inject[Int](identified by "errors.auth.accessDenied")
        ))
        case e: AuthenticationException => BadRequest(Json.obj(
          "code" -> inject[Int](identified by "errors.auth.notAuthenticated")
        ))
      }
      case errors: JsError => Future.successful(BadRequest(Json.obj(
        "code" -> inject[Int](identified by "errors.auth.loginPasswordNotValid"),
        "fields" -> JsError.toFlatJson(errors)
      )))
    }

  }

  def credentialsAuthentication(userCredentials: Credentials) = env.providers.get(CredentialsProvider.Credentials) match {
    case Some(credentialsProvider: CredentialsProvider) => credentialsProvider.authenticate(userCredentials)
    case _ => Future.failed(new AuthenticationException(s"Cannot find credentials provider"))
  }

  def socialAuthenticationHandler(provider: String) = Action.async { implicit request =>
    (env.providers.get(provider) match {
      case Some(p: SocialProvider[_] with CommonSocialProfileBuilder[_]) => p.authenticate()
      case _ => Future.failed(new AuthenticationException(s"Cannot authenticate with unexpected social provider $provider"))
    }).flatMap {
      case Left(result) => Future.successful(result)
      case Right(profile: CommonSocialProfile[_]) =>
        for {
          user <- userService.save(profile)
          authInfo <- authInfoService.save(profile.loginInfo, profile.authInfo)
          maybeAuthenticator <- env.authenticatorService.create(user)
        } yield {
          maybeAuthenticator match {
            case Some(authenticator) =>
              env.eventBus.publish(LoginEvent(user, request, request2lang))
              env.authenticatorService.send(authenticator, Redirect(routes.Application.index()))
            case None => throw new AuthenticationException("Couldn't create an authenticator")
          }
        }
    }.recoverWith(exceptionHandler)
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
        val loginInfo = LoginInfo(CredentialsProvider.Credentials, email)
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

  def createNewUser(loginInfo: LoginInfo, email: String, password: PasswordInfo) = {
    val user = User(
      id = new ObjectId(),
      loginInfo = loginInfo,
      email = Some(email),
      avatarURL = None
    )

    val rootFolder = RootFolder(
      folder = Folder(inject[String](identified by "folder.rootFolderName"), List())
    )

    for {
      avatar <- avatarService.retrieveURL(email)
      user <- userService.save(user.copy(avatarURL = avatar))
      rootFolder <- rootFolderService.save(rootFolder.copy(userId = user.id))
      authInfo <- authInfoService.save(loginInfo, password)
      maybeAuthenticator <- env.authenticatorService.create(user)
    } yield {
      maybeAuthenticator match {
        case Some(authenticator) =>
          env.authenticatorService.send(authenticator, Ok(Json.obj("identity" -> user.email)))
        case None => BadRequest(Json.obj(
          "code" -> inject[Int](identified by "errors.auth.noAuthenticator")
        ))
      }
    }

  }
}
