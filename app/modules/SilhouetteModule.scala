package modules

import com.mohiva.play.silhouette.api.{Environment, EventBus}
import com.mohiva.play.silhouette.api.services.{AuthenticatorService, AvatarService, AuthInfoService}
import com.mohiva.play.silhouette.api.util._
import com.mohiva.play.silhouette.impl.authenticators._
import com.mohiva.play.silhouette.impl.daos.DelegableAuthInfoDAO
import com.mohiva.play.silhouette.impl.providers.credentials.hasher.BCryptPasswordHasher
import com.mohiva.play.silhouette.impl.providers._
import com.mohiva.play.silhouette.impl.services.{GravatarService, DelegableAuthInfoService}
import com.mohiva.play.silhouette.impl.util.{DefaultFingerprintGenerator, SecureRandomIDGenerator, PlayCacheLayer}
import models._
import models.silhouette.{OAuth1InfoDAO, OAuth2InfoDAO, PasswordInfoDAO}
import scaldi.Module
import services.{User, UserService}

/**
 * Silhouette module dependency injections
 */
class SilhouetteModule extends Module {

  bind[PasswordInfoDAO] to new PasswordInfoDAO
  bind[OAuth1InfoDAO] to new OAuth1InfoDAO
  bind[OAuth2InfoDAO] to new OAuth2InfoDAO
  bind[UserDAO] to new UserDAO
  bind[UserService] to new UserService
  bind[DelegableAuthInfoDAO[PasswordInfo]] to new PasswordInfoDAO
  bind[DelegableAuthInfoDAO[OAuth1Info]] to new OAuth1InfoDAO
  bind[DelegableAuthInfoDAO[OAuth2Info]] to new OAuth2InfoDAO
  bind[CacheLayer] to new PlayCacheLayer
  bind[HTTPLayer] to new PlayHTTPLayer
  bind[IDGenerator] to new SecureRandomIDGenerator
  bind[FingerprintGenerator] to new DefaultFingerprintGenerator(false)
  bind[PasswordHasher] to new BCryptPasswordHasher
  bind[EventBus] to new EventBus



  binding toProvider new SessionAuthenticatorService(SessionAuthenticatorSettings(

    sessionKey = inject[String]("silhouette.authenticator.sessionKey"),
    encryptAuthenticator = inject[Boolean]("silhouette.authenticator.encryptAuthenticator"),
    useFingerprinting = inject[Boolean]("silhouette.authenticator.useFingerprinting"),
    authenticatorIdleTimeout = Option(inject[Int]("silhouette.authenticator.authenticatorIdleTimeout")),
    authenticatorExpiry = inject[Int]("silhouette.authenticator.authenticatorExpiry")
  ), inject[FingerprintGenerator], Clock())

  bind[AuthInfoService] toProvider new DelegableAuthInfoService(inject[PasswordInfoDAO], inject[OAuth1InfoDAO], inject[OAuth2InfoDAO])

  bind[AvatarService] toProvider new GravatarService(inject[HTTPLayer])

  // Auth providers //
  bind[CredentialsProvider] toProvider new CredentialsProvider(inject[AuthInfoService], inject[PasswordHasher], Seq(inject[PasswordHasher]))

  // Main env injection in each Controller //
  bind[Environment[User, SessionAuthenticator]] toProvider {
    val credentialsProvider = inject[CredentialsProvider]

    Environment[User, SessionAuthenticator](
      inject[UserService],
      inject[AuthenticatorService[SessionAuthenticator]],
      Map(
        credentialsProvider.id -> credentialsProvider
      ),
      inject[EventBus]
    )
  }
}
