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

import com.typesafe.config.{Config, ConfigFactory}

object BenchmarkConfig {
  val config: BenchmarkConfig = apply()

  def apply(): BenchmarkConfig = {
    val config: Config = ConfigFactory.load().withFallback(ConfigFactory.load("benchmark-defaults"))

    val http: Config = config.getConfig("http")
    val auth: Config = config.getConfig("auth")
    val dataset: Config = config.getConfig("dataset.tree")
    val workload: Config = config.getConfig("workload")

    val connectionParams = ConnectionParameters(
      http.getString("base-url"),
      http.getString("api-prefix")
    )

    val authType = AuthType.fromString(auth.getString("type"))
    val sigv4Params = if (authType == AuthType.Sigv4) {
      val sigv4Config = auth.getConfig("sigv4")
      Some(
        Sigv4Parameters(
          sigv4Config.getString("access-key"),
          sigv4Config.getString("secret-key"),
          sigv4Config.getString("region"),
          sigv4Config.getString("service")
        )
      )
    } else {
      None
    }

    def getOptionalString(config: Config, path: String): Option[String] =
      if (config.hasPath(path) && !config.getIsNull(path)) {
        Some(config.getString(path)).filter(_.nonEmpty)
      } else {
        None
      }

    val authParams = AuthParameters(
      authType,
      getOptionalString(auth, "client-id"),
      getOptionalString(auth, "client-secret"),
      auth.getInt("refresh-interval-seconds"),
      auth.getInt("max-retries"),
      auth.getIntList("retryable-http-codes").toArray.map(_.asInstanceOf[Int]).toSet,
      sigv4Params
    )

    val workloadParams = {
      val ccConfig = workload.getConfig("create-commits")
      val rtdConfig = workload.getConfig("read-tree-dataset")
      val ctdConfig = workload.getConfig("create-tree-dataset")
      val rutdConfig = workload.getConfig("read-update-tree-dataset")
      val wwotdConfig = workload.getConfig("weighted-workload-on-tree-dataset")

      WorkloadParameters(
        CreateCommitsParameters(
          ccConfig.getInt("table-commits-throughput"),
          ccConfig.getInt("view-commits-throughput"),
          ccConfig.getInt("duration-in-minutes")
        ),
        ReadTreeDatasetParameters(
          rtdConfig.getInt("namespace-concurrency"),
          rtdConfig.getInt("table-concurrency"),
          rtdConfig.getInt("view-concurrency")
        ),
        CreateTreeDatasetParameters(
          ctdConfig.getInt("table-concurrency"),
          ctdConfig.getInt("view-concurrency")
        ),
        ReadUpdateTreeDatasetParameters(
          rutdConfig.getDouble("read-write-ratio"),
          rutdConfig.getInt("throughput"),
          rutdConfig.getInt("duration-in-minutes")
        ),
        WeightedWorkloadOnTreeDatasetParameters(
          wwotdConfig.getInt("seed"),
          WeightedWorkloadOnTreeDatasetParameters.loadDistributionsList(wwotdConfig, "readers"),
          WeightedWorkloadOnTreeDatasetParameters.loadDistributionsList(wwotdConfig, "writers"),
          wwotdConfig.getInt("duration-in-minutes")
        )
      )
    }

    val datasetParams = DatasetParameters(
      dataset.getBoolean("skip-catalog-creation"),
      dataset.getString("catalog-name"),
      dataset.getInt("num-catalogs"),
      dataset.getString("default-base-location"),
      dataset.getInt("namespace-width"),
      dataset.getInt("namespace-depth"),
      dataset.getInt("namespace-properties"),
      dataset.getInt("tables-per-namespace"),
      dataset.getInt("max-tables"),
      dataset.getInt("columns-per-table"),
      dataset.getInt("table-properties"),
      dataset.getInt("views-per-namespace"),
      dataset.getInt("max-views"),
      dataset.getInt("columns-per-view"),
      dataset.getInt("view-properties"),
      dataset.getString("storage-config-info"),
      dataset.getBoolean("mangle-names")
    )

    BenchmarkConfig(connectionParams, authParams, workloadParams, datasetParams)
  }
}

case class BenchmarkConfig(
    connectionParameters: ConnectionParameters,
    authParameters: AuthParameters,
    workloadParameters: WorkloadParameters,
    datasetParameters: DatasetParameters
) {}
