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
package org.apache.polaris.benchmarks.parameters

sealed trait AuthType
object AuthType {
  case object OAuth2 extends AuthType
  case object Sigv4 extends AuthType

  def fromString(s: String): AuthType = s.toLowerCase match {
    case "oauth2" => OAuth2
    case "sigv4"  => Sigv4
    case other    => throw new IllegalArgumentException(s"Unknown auth type: $other")
  }
}

case class Sigv4Parameters(
    accessKey: String,
    secretKey: String,
    region: String,
    service: String
) {
  require(accessKey != null && accessKey.nonEmpty, "SIGV4 access key cannot be null or empty")
  require(secretKey != null && secretKey.nonEmpty, "SIGV4 secret key cannot be null or empty")
  require(region != null && region.nonEmpty, "SIGV4 region cannot be null or empty")
  require(service != null && service.nonEmpty, "SIGV4 service cannot be null or empty")
}

case class AuthParameters(
    authType: AuthType,
    clientId: Option[String],
    clientSecret: Option[String],
    refreshIntervalSeconds: Int,
    maxRetries: Int,
    retryableHttpCodes: Set[Int],
    sigv4: Option[Sigv4Parameters]
) {
  require(refreshIntervalSeconds > 0, "Refresh interval must be positive")
  require(maxRetries >= 0, "Max retries cannot be negative")
  require(retryableHttpCodes != null, "Retryable HTTP codes cannot be null")

  authType match {
    case AuthType.OAuth2 =>
      require(
        clientId.isDefined && clientId.get.nonEmpty,
        "Client ID is required for OAuth2 auth"
      )
      require(
        clientSecret.isDefined && clientSecret.get.nonEmpty,
        "Client secret is required for OAuth2 auth"
      )
    case AuthType.Sigv4 =>
      require(sigv4.isDefined, "SIGV4 parameters are required for SIGV4 auth")
  }

  def isOAuth2: Boolean = authType == AuthType.OAuth2
  def isSigv4: Boolean = authType == AuthType.Sigv4
}
