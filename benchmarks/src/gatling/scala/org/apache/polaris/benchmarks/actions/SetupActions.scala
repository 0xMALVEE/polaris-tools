/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.polaris.benchmarks.actions

import io.gatling.core.Predef._
import io.gatling.core.structure.{ChainBuilder, ScenarioBuilder}
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder
import org.apache.polaris.benchmarks.auth.Sigv4RequestSigner
import org.apache.polaris.benchmarks.parameters.{AuthParameters, AuthType, ConnectionParameters}

import java.util.concurrent.atomic.{AtomicBoolean, AtomicReference}
import scala.concurrent.duration._

/**
 * Actions for setting up necessary shared states like authentication across benchmark simulations.
 *
 * @param cp Connection parameters containing the base URL
 * @param ap Authentication parameters containing client credentials and retry settings
 */
case class SetupActions(
    cp: ConnectionParameters,
    ap: AuthParameters
) {

  /**
   * Shared access token reference that can be passed to all action classes.
   * For SIGV4 auth, this is set to a placeholder value since signing happens per-request.
   */
  val accessToken: AtomicReference[String] = new AtomicReference()

  /**
   * Internal flag to control the token refresh loop.
   */
  private val shouldRefreshToken: AtomicBoolean = new AtomicBoolean(true)

  /**
   * Authentication actions instance that handles the actual OAuth token operations.
   * Only used for OAuth2 authentication.
   */
  private lazy val authActions: AuthenticationActions =
    AuthenticationActions(cp, ap, accessToken)

  /**
   * SIGV4 request signer for AWS-style authentication.
   * Only used when auth type is SIGV4.
   */
  lazy val sigv4Signer: Option[Sigv4RequestSigner] = ap.sigv4.map(Sigv4RequestSigner(_))

  /**
   * Returns true if using SIGV4 authentication.
   */
  def isSigv4Auth: Boolean = ap.isSigv4

  /**
   * Returns true if using OAuth2 authentication.
   */
  def isOAuth2Auth: Boolean = ap.isOAuth2

  /**
   * Configures the HTTP protocol with appropriate authentication.
   * For OAuth2: No special protocol config needed (uses Bearer token in headers)
   * For SIGV4: Adds SignatureCalculator to sign each request
   */
  def configureHttpProtocol(baseProtocol: HttpProtocolBuilder): HttpProtocolBuilder = {
    ap.authType match {
      case AuthType.Sigv4 =>
        sigv4Signer match {
          case Some(signer) => baseProtocol.sign(signer.signFunction)
          case None =>
            throw new IllegalStateException("SIGV4 signer not configured")
        }
      case AuthType.OAuth2 =>
        baseProtocol
    }
  }

  /**
   * Continuously refreshes the OAuth token at the configured interval specified in
   * [[AuthParameters.refreshIntervalSeconds]]. This scenario runs indefinitely in a loop controlled
   * by the shouldRefreshToken flag until [[stopRefreshingToken]] is called.
   *
   * For SIGV4 auth, returns a no-op scenario since signing happens per-request.
   *
   * @return ScenarioBuilder that continuously refreshes the token
   */
  def continuouslyRefreshOauthToken(): ScenarioBuilder = {
    if (ap.isSigv4) {
      scenario("SIGV4 auth - no token refresh needed")
        .exec { session =>
          accessToken.set("sigv4-placeholder")
          session.set("accessToken", "sigv4-placeholder")
        }
        .asLongAs(_ => shouldRefreshToken.get()) {
          pause(ap.refreshIntervalSeconds.seconds)
        }
    } else {
      val interval = ap.refreshIntervalSeconds.seconds
      scenario(s"Authenticate every ${interval.toSeconds}s using the Iceberg REST API")
        .asLongAs(_ => shouldRefreshToken.get()) {
          feed(authActions.feeder())
            .exec(authActions.authenticateAndSaveAccessToken)
            .pause(interval)
        }
    }
  }

  /**
   * Refreshes the OAuth token at the configured interval specified in
   * [[AuthParameters.refreshIntervalSeconds]] for a specified duration. Unlike
   * [[continuouslyRefreshOauthToken]], this method automatically stops after the duration expires
   * without requiring an explicit stop call.
   *
   * For SIGV4 auth, returns a no-op scenario since signing happens per-request.
   *
   * @param duration Total duration to keep refreshing the token
   * @return ScenarioBuilder that refreshes the token for the specified duration
   */
  def refreshOauthForDuration(duration: FiniteDuration): ScenarioBuilder = {
    if (ap.isSigv4) {
      scenario("SIGV4 auth - no token refresh needed")
        .exec { session =>
          accessToken.set("sigv4-placeholder")
          session.set("accessToken", "sigv4-placeholder")
        }
        .during(duration) {
          pause(ap.refreshIntervalSeconds.seconds)
        }
    } else {
      val interval = ap.refreshIntervalSeconds.seconds
      scenario(s"Authenticate every ${interval.toSeconds}s using the Iceberg REST API")
        .during(duration) {
          feed(authActions.feeder())
            .exec(authActions.authenticateAndSaveAccessToken)
            .pause(interval)
        }
    }
  }

  /**
   * Waits for the authentication token to be available before proceeding. This is useful when the
   * authentication is performed by a separate scenario. This operation does not make any network
   * requests.
   *
   * For SIGV4 auth, immediately sets the placeholder token and continues.
   *
   * @return ScenarioBuilder that waits for token availability
   */
  val waitForAuthentication: ScenarioBuilder = {
    if (ap.isSigv4) {
      scenario("SIGV4 auth ready")
        .exec { session =>
          accessToken.set("sigv4-placeholder")
          session.set("accessToken", "sigv4-placeholder")
        }
    } else {
      scenario("Wait for the authentication token to be available")
        .asLongAs(_ => accessToken.get() == null) {
          pause(1.second)
        }
    }
  }

  /**
   * Stops the token refresh loop. This is useful when the authentication is performed by a separate
   * scenario. This operation does not make any network requests.
   *
   * @return ScenarioBuilder that stops the token refresh
   */
  val stopRefreshingToken: ScenarioBuilder =
    scenario("Stop refreshing the authentication token")
      .exec { session =>
        shouldRefreshToken.set(false)
        session
      }

  /**
   * Restores the access token from the shared reference to the Gatling session. This is useful when
   * the authentication is performed by a separate scenario. This operation does not make any
   * network requests.
   *
   * @return ChainBuilder that restores the token to the session
   */
  val restoreAccessTokenInSession: ChainBuilder =
    exec(session => session.set("accessToken", accessToken.get()))
}
