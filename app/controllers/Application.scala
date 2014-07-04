package controllers

import _root_.services.UserService
import com.mohiva.play.silhouette.contrib.services.CachedCookieAuthenticator
import com.mohiva.play.silhouette.core._
import com.mohiva.play.silhouette.core.exceptions.AuthenticationException
import com.mohiva.play.silhouette.core.providers._
import com.mohiva.play.silhouette.core.services.{AvatarService, AuthInfoService}
import com.mohiva.play.silhouette.core.utils.PasswordHasher
import forms._
import models.User
import org.bson.types.ObjectId
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc.Action
import scaldi.{Injectable, Injector}

import scala.concurrent.Future

/**
 * The basic application controller.
 */
class Application(implicit inj: Injector)
  extends Silhouette[User, CachedCookieAuthenticator] with Injectable {

  implicit val env = inject[Environment[User, CachedCookieAuthenticator]]

  val userService = inject[UserService]
  val authInfoService = inject[AuthInfoService]
  val avatarService = inject[AvatarService]
  val passwordHasher = inject[PasswordHasher]

  /**
   * Handles the index action.
   *
   * @return The result to display.
   */
  def index = SecuredAction.async { implicit request =>
    Future.successful(Ok(views.html.index(request.identity)))
  }

  /**
   * Handles the Sign In action.
   *
   * @return The result to display.
   */
  def signIn = UserAwareAction.async { implicit request =>
    request.identity match {
      case Some(user) => Future.successful(Redirect(routes.Application.index))
      case None => Future.successful(Ok(views.html.signIn(SignInForm.form)))
    }
  }

  /**
   * Handles the Sign Up action.
   *
   * @return The result to display.
   */
  def signUp = UserAwareAction.async { implicit request =>
    request.identity match {
      case Some(user) => Future.successful(Redirect(routes.Application.index))
      case None => Future.successful(Ok(views.html.signUp(SignUpForm.form)))
    }
  }

  /**
   * Handles the Sign Out action.
   *
   * @return The result to display.
   */
  def signOut = SecuredAction.async { implicit request =>
    env.eventBus.publish(LogoutEvent(request.identity, request, request2lang))
    Future.successful(env.authenticatorService.discard(Redirect(routes.Application.index)))
  }

  /**
   * Authenticates a user against the credentials provider.
   *
   * @return The result to display.
   */
  def credentialsAuthenticationHandler = Action.async { implicit request => SignInForm.form.bindFromRequest.fold(

    formWithErrors => Future.successful(BadRequest(views.html.signIn(formWithErrors))),

    userCredentials => credentialsAuthentication(userCredentials).flatMap { loginInfo =>
      userService.retrieve(loginInfo).flatMap {
        case Some(user) => env.authenticatorService.create(user).map {
          case Some(authenticator) =>
            env.eventBus.publish(LoginEvent(user, request, request2lang))
            env.authenticatorService.send(authenticator, Redirect(routes.Application.index))
          case None => throw new AuthenticationException("Couldn't create an authenticator")
        }
        case None => Future.failed(new AuthenticationException("Couldn't find user"))
      }
    }.recoverWith(exceptionHandler)
  )
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
              env.authenticatorService.send(authenticator, Redirect(routes.Application.index))
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
  def signUpHandler = Action.async { implicit request =>
    SignUpForm.form.bindFromRequest.fold(

      formWithErrors => Future.successful(BadRequest(views.html.signUp(formWithErrors))),

      data => {
        val loginInfo = LoginInfo(CredentialsProvider.Credentials, data.email)
        val authInfo = passwordHasher.hash(data.password)
        val user = User(
          userID = new ObjectId(),
          loginInfo = loginInfo,
          firstName = Some(data.firstName),
          lastName = Some(data.lastName),
          fullName = Some(data.firstName + " " + data.lastName),
          email = Some(data.email),
          avatarURL = None
        )

        for {
          avatar <- avatarService.retrieveURL(data.email)
          user <- userService.save(user.copy(avatarURL = avatar))
          authInfo <- authInfoService.save(loginInfo, authInfo)
          maybeAuthenticator <- env.authenticatorService.create(user)
        } yield {
          maybeAuthenticator match {
            case Some(authenticator) =>
              env.eventBus.publish(SignUpEvent(user, request, request2lang))
              env.eventBus.publish(LoginEvent(user, request, request2lang))
              env.authenticatorService.send(authenticator, Redirect(routes.Application.index))
            case None => throw new AuthenticationException("Couldn't create an authenticator")
          }
        }
      }
    )
  }
}
