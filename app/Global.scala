import com.mohiva.play.silhouette.core.{Logger, SecuredSettings}
import controllers.routes
import modules.{SilhouetteModule, WebModule}
import play.api.GlobalSettings
import play.api.i18n.{Lang, Messages}
import play.api.mvc.Results._
import play.api.mvc.{RequestHeader, Result}
import scaldi.play.{ControllerInjector, ScaldiSupport}

import scala.concurrent.Future

/**
 * The global configuration.
 */
object Global extends GlobalSettings with ScaldiSupport with SecuredSettings with Logger {

  def applicationModule = new WebModule :: new SilhouetteModule :: new ControllerInjector


  /**
   * Called when a user is not authenticated.
   *
   * As defined by RFC 2616, the status code of the response should be 401 Unauthorized.
   *
   * @param request The request header.
   * @param lang The currently selected language.
   * @return The result to send to the client.
   */
  override def onNotAuthenticated(request: RequestHeader, lang: Lang): Option[Future[Result]] = {
    Some(Future.successful(Redirect(routes.Application.index())))
  }

  /**
   * Called when a user is authenticated but not authorized.
   *
   * As defined by RFC 2616, the status code of the response should be 403 Forbidden.
   *
   * @param request The request header.
   * @param lang The currently selected language.
   * @return The result to send to the client.
   */
  override def onNotAuthorized(request: RequestHeader, lang: Lang): Option[Future[Result]] = {
    Some(Future.successful(Redirect(routes.Application.index()).flashing("error" -> Messages("access.denied"))))
  }
}
