package controllers


import com.mohiva.play.silhouette.api.Silhouette
import com.mohiva.play.silhouette.impl.authenticators.SessionAuthenticator
import models.User
import play.api.mvc._

import scala.concurrent.Future

/**
 * Every controller should extend this trait.
 * Contains shared methods.
 */
trait Controller extends Silhouette[User, SessionAuthenticator] {

  /**
   * Action will redirect user to the login page if his/her identity is not found.
   * @param f function taking User as parameter and returning Result
   * @return Action
   */
  protected def authenticatedAction(f: User => Result): Action[AnyContent] = UserAwareAction { implicit request =>
    request.identity match {
      case Some(user) => f(user)
      case None => Redirect(routes.Application.index())
    }
  }

  /**
   * Action will redirect user to the login page if his/her identity is not found (async version).
   * @param f function taking User as parameter and returning Future of Result
   * @return Action
   */
  protected def authenticatedActionAsync(f: User => Future[Result]): Action[AnyContent] = UserAwareAction.async { implicit request =>
    request.identity match {
      case Some(user) => f(user)
      case None => Future.successful(Redirect(routes.Application.index()))
    }
  }

  /**
   * Action will redirect user to the login page if his/her identity is not found (async version).
   * You should provide corresponding parser (json, multipart, etc.)
   * @param parser BodyParser to parse data
   * @param f function taking Request and User as parameters and returning Future of Result
   * @tparam T type of the data that should be parsed (json, multipart, int, etc.)
   * @return Action
   */
  protected def authenticatedActionParseAsync[T](parser: BodyParser[T])(f: (UserAwareRequest[T], User) => Future[Result]): Action[T] =
    UserAwareAction.async(parser) { implicit request =>
      request.identity match {
        case Some(user) => f(request, user)
        case None => Future.successful(Redirect(routes.Application.index()))
      }
    }
}
