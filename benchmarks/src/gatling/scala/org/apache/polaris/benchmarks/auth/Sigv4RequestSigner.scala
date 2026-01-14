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

import io.gatling.commons.validation.{Success, Validation}
import io.gatling.core.session.Session
import io.gatling.http.client.Request
import org.apache.polaris.benchmarks.parameters.Sigv4Parameters
import org.slf4j.LoggerFactory

import java.nio.charset.StandardCharsets

class Sigv4RequestSigner(params: Sigv4Parameters) {
  private val logger = LoggerFactory.getLogger(getClass)

  private val signer = Sigv4Signer(
    params.accessKey,
    params.secretKey,
    params.region,
    params.service
  )

  def sign(request: Request, session: Session): Validation[Request] = {
    val method = request.getMethod.name()
    val url = request.getUri.toUrl
    val body = Option(request.getBody)
      .map(b => new String(b.getBytes, StandardCharsets.UTF_8))

    val existingHeaders = scala.collection.mutable.Map[String, String]()
    val headerIterator = request.getHeaders.iteratorAsString()
    while (headerIterator.hasNext) {
      val entry = headerIterator.next()
      if (entry.getKey.toLowerCase != "authorization") {
        existingHeaders.put(entry.getKey, entry.getValue)
      }
    }

    val signed = signer.sign(method, url, existingHeaders.toMap, body)

    request.getHeaders.set("Authorization", signed.authorization)
    request.getHeaders.set("X-Amz-Date", signed.xAmzDate)
    request.getHeaders.set("X-Amz-Content-Sha256", signed.xAmzContentSha256)

    logger.info(s"SIGV4 signed request: $method $url | Body: ${body.map(_.replace("\n", " ").take(200)).getOrElse("<empty>")}")

    Success(request)
  }

  def signFunction: (Request, Session) => Validation[Request] = sign
}

object Sigv4RequestSigner {
  def apply(params: Sigv4Parameters): Sigv4RequestSigner = new Sigv4RequestSigner(params)
}
