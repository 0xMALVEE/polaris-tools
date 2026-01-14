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
package org.apache.polaris.benchmarks.auth

import java.net.{URI, URLEncoder}
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.time.format.DateTimeFormatter
import java.time.{ZoneOffset, ZonedDateTime}
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

case class Sigv4SignedRequest(
    authorization: String,
    xAmzDate: String,
    xAmzContentSha256: String
)

case class Sigv4Signer(
    accessKey: String,
    secretKey: String,
    region: String,
    service: String
) {
  private val algorithm = "AWS4-HMAC-SHA256"
  private val dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'")
  private val shortDateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")

  def sign(
      method: String,
      url: String,
      headers: Map[String, String],
      body: Option[String]
  ): Sigv4SignedRequest = {
    val now = ZonedDateTime.now(ZoneOffset.UTC)
    val amzDate = now.format(dateFormatter)
    val dateStamp = now.format(shortDateFormatter)

    val uri = new URI(url)
    val host = uri.getHost + (if (uri.getPort > 0) s":${uri.getPort}" else "")
    val path = Option(uri.getRawPath).filter(_.nonEmpty).getOrElse("/")
    val queryString = Option(uri.getRawQuery).getOrElse("")

    val bodyHash = hash(body.getOrElse(""))

    val allHeaders = headers ++ Map(
      "host" -> host,
      "x-amz-date" -> amzDate,
      "x-amz-content-sha256" -> bodyHash
    )

    val sortedHeaders = allHeaders.toSeq.sortBy(_._1.toLowerCase)
    val signedHeaders = sortedHeaders.map(_._1.toLowerCase).mkString(";")
    val canonicalHeaders = sortedHeaders
      .map { case (k, v) => s"${k.toLowerCase}:${v.trim}" }
      .mkString("\n") + "\n"

    val canonicalQueryString = if (queryString.isEmpty) {
      ""
    } else {
      queryString
        .split("&")
        .map { param =>
          val parts = param.split("=", 2)
          val key = URLEncoder.encode(parts(0), StandardCharsets.UTF_8.name())
          val value =
            if (parts.length > 1) URLEncoder.encode(parts(1), StandardCharsets.UTF_8.name())
            else ""
          s"$key=$value"
        }
        .sorted
        .mkString("&")
    }

    val canonicalRequest = Seq(
      method.toUpperCase,
      path,
      canonicalQueryString,
      canonicalHeaders,
      signedHeaders,
      bodyHash
    ).mkString("\n")

    val credentialScope = s"$dateStamp/$region/$service/aws4_request"
    val stringToSign = Seq(
      algorithm,
      amzDate,
      credentialScope,
      hash(canonicalRequest)
    ).mkString("\n")

    val signingKey = getSignatureKey(secretKey, dateStamp, region, service)
    val signature = hmacSha256Hex(signingKey, stringToSign)

    val authorization =
      s"$algorithm Credential=$accessKey/$credentialScope, SignedHeaders=$signedHeaders, Signature=$signature"

    Sigv4SignedRequest(authorization, amzDate, bodyHash)
  }

  private def hash(text: String): String = {
    val digest = MessageDigest.getInstance("SHA-256")
    val hash = digest.digest(text.getBytes(StandardCharsets.UTF_8))
    hash.map("%02x".format(_)).mkString
  }

  private def hmacSha256(key: Array[Byte], data: String): Array[Byte] = {
    val mac = Mac.getInstance("HmacSHA256")
    mac.init(new SecretKeySpec(key, "HmacSHA256"))
    mac.doFinal(data.getBytes(StandardCharsets.UTF_8))
  }

  private def hmacSha256Hex(key: Array[Byte], data: String): String = {
    hmacSha256(key, data).map("%02x".format(_)).mkString
  }

  private def getSignatureKey(
      key: String,
      dateStamp: String,
      region: String,
      service: String
  ): Array[Byte] = {
    val kSecret = ("AWS4" + key).getBytes(StandardCharsets.UTF_8)
    val kDate = hmacSha256(kSecret, dateStamp)
    val kRegion = hmacSha256(kDate, region)
    val kService = hmacSha256(kRegion, service)
    hmacSha256(kService, "aws4_request")
  }
}
