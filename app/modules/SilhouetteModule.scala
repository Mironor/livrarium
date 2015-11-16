package modules

import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services.{AuthenticatorService, AvatarService}
import com.mohiva.play.silhouette.api.util._
import com.mohiva.play.silhouette.api.{Environment, EventBus}
import com.mohiva.play.silhouette.impl.authenticators._
import com.mohiva.play.silhouette.impl.daos.DelegableAuthInfoDAO
import com.mohiva.play.silhouette.impl.providers._
import com.mohiva.play.silhouette.impl.repositories.DelegableAuthInfoRepository
import com.mohiva.play.silhouette.impl.services.GravatarService
import com.mohiva.play.silhouette.impl.util.{BCryptPasswordHasher, DefaultFingerprintGenerator, PlayCacheLayer, SecureRandomIDGenerator}
import daos.UserDAO
import daos.silhouette.{LoginInfoDAO, OAuth1InfoDAO, OAuth2InfoDAO, PasswordInfoDAO}
import models._
import play.api.cache.CacheApi
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.ws.WSClient
import scaldi.Module
import services.UserService

import scala.concurrent.duration
import scala.concurrent.duration.FiniteDuration

/**
 * Silhouette module dependency injections
 */
class SilhouetteModule extends Module {

  bind[UserService] to new UserService
  bind[UserDAO] to new UserDAO
  bind[DelegableAuthInfoDAO[PasswordInfo]] to new PasswordInfoDAO
  bind[DelegableAuthInfoDAO[OAuth1Info]] to new OAuth1InfoDAO
  bind[DelegableAuthInfoDAO[OAuth2Info]] to new OAuth2InfoDAO
  bind[CacheLayer] to new PlayCacheLayer(inject[CacheApi])
  bind[IDGenerator] to new SecureRandomIDGenerator
  bind[PasswordHasher] to new BCryptPasswordHasher
  bind[FingerprintGenerator] to new DefaultFingerprintGenerator(false)
  bind[EventBus] to new EventBus

  bind[LoginInfoDAO] to new LoginInfoDAO
  bind[PasswordInfoDAO] to new PasswordInfoDAO
  bind[OAuth1InfoDAO] to new OAuth1InfoDAO
  bind[OAuth2InfoDAO] to new OAuth2InfoDAO

  bind[HTTPLayer] toProvider new PlayHTTPLayer(inject[WSClient])

  binding toProvider new SessionAuthenticatorService(SessionAuthenticatorSettings(
    sessionKey = inject[String]("silhouette.authenticator.sessionKey"),
    encryptAuthenticator = inject[Boolean]("silhouette.authenticator.encryptAuthenticator"),
    useFingerprinting = inject[Boolean]("silhouette.authenticator.useFingerprinting"),
    authenticatorIdleTimeout = Option(FiniteDuration(inject[Int]("silhouette.authenticator.authenticatorIdleTimeout"), duration.SECONDS)),
    authenticatorExpiry = FiniteDuration(inject[Int]("silhouette.authenticator.authenticatorExpiry"), duration.SECONDS)
  ), inject[FingerprintGenerator], Clock())

  bind[AuthInfoRepository] toProvider new DelegableAuthInfoRepository(inject[PasswordInfoDAO], inject[OAuth1InfoDAO], inject[OAuth2InfoDAO])

  bind[AvatarService] toProvider new GravatarService(inject[HTTPLayer])

  // Auth providers //
  bind[CredentialsProvider] toProvider new CredentialsProvider(inject[AuthInfoRepository], inject[PasswordHasher], Seq(inject[PasswordHasher]))

  // Main env injection in each Controller //
  bind[Environment[User, SessionAuthenticator]] toProvider Environment[User, SessionAuthenticator](
    inject[UserService],
    inject[AuthenticatorService[SessionAuthenticator]],
    Seq(),
    inject[EventBus]
  )
}
