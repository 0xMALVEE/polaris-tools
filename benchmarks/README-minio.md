<!--
  Licensed to the Apache Software Foundation (ASF) under one
  or more contributor license agreements.  See the NOTICE file
  distributed with this work for additional information
  regarding copyright ownership.  The ASF licenses this file
  to you under the Apache License, Version 2.0 (the
  "License"); you may not use this file except in compliance
  with the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing,
  software distributed under the License is distributed on an
  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  KIND, either express or implied.  See the License for the
  specific language governing permissions and limitations
  under the License.
-->

# Benchmarking MinIO S3 Tables (Iceberg REST Catalog)

This guide explains how to run Gatling load tests against MinIO S3 Tables using SIGV4 authentication.

## Prerequisites

- Java 21 (Java 25 has compatibility issues with Gradle)
- MinIO with S3 Tables enabled
- A pre-existing table bucket (catalog) in MinIO

```bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk
```

## Configuration

Create `application.conf` in the `benchmarks/` directory:

```hocon
http {
  base-url = "http://localhost:9001/_iceberg"
  api-prefix = "/v1"
}

auth {
  type = "sigv4"
  sigv4 {
    access-key = "minioadmin"
    secret-key = "minioadmin"
    region = "us-east-1"
    service = "s3tables"
  }
}

dataset.tree {
  skip-catalog-creation = true
  catalog-name = "your-bucket-name"
  default-base-location = "s3://your-bucket-name"
  
  # Dataset size parameters
  namespace-width = 2
  namespace-depth = 2
  tables-per-namespace = 5
  views-per-namespace = 2
  
  # MinIO requires lowercase property keys, set to 0 to avoid issues
  namespace-properties = 0
  table-properties = 0
  view-properties = 0
}
```

## Available Simulations

| Simulation | Description |
|------------|-------------|
| `CreateTreeDataset` | Creates namespaces, tables, and views |
| `ReadTreeDataset` | Reads existing namespaces, tables, and views |
| `ReadUpdateTreeDataset` | Mixed read/write workload |
| `CreateCommits` | Creates commits on existing tables/views |
| `WeightedWorkloadOnTreeDataset` | Weighted distribution of readers/writers |

## Quick Start Commands

### Using Make (Recommended)

```bash
# Create dataset (namespaces, tables, views)
make minio-create

# Read dataset
make minio-read

# Mixed read/write workload
make minio-read-update

# Create commits on tables/views
make minio-commits

# Weighted workload simulation
make minio-weighted
```

### Using Gradle Directly

```bash
# Create dataset
./gradlew gatlingRun --simulation org.apache.polaris.benchmarks.simulations.CreateTreeDataset

# Read dataset
./gradlew gatlingRun --simulation org.apache.polaris.benchmarks.simulations.ReadTreeDataset

# Mixed read/write
./gradlew gatlingRun --simulation org.apache.polaris.benchmarks.simulations.ReadUpdateTreeDataset

# Create commits
./gradlew gatlingRun --simulation org.apache.polaris.benchmarks.simulations.CreateCommits

# Weighted workload
./gradlew gatlingRun --simulation org.apache.polaris.benchmarks.simulations.WeightedWorkloadOnTreeDataset
```

## Load Profiles

### Light Load (Development/Testing)

```hocon
dataset.tree {
  namespace-width = 2
  namespace-depth = 1
  tables-per-namespace = 2
  views-per-namespace = 1
}

workload {
  create-tree-dataset {
    table-concurrency = 5
    view-concurrency = 2
  }
  read-tree-dataset {
    namespace-concurrency = 5
    table-concurrency = 5
    view-concurrency = 2
  }
}
```

**Make target:** `make minio-create-light`

### Medium Load (Staging)

```hocon
dataset.tree {
  namespace-width = 3
  namespace-depth = 3
  tables-per-namespace = 10
  views-per-namespace = 5
}

workload {
  create-tree-dataset {
    table-concurrency = 20
    view-concurrency = 10
  }
  read-tree-dataset {
    namespace-concurrency = 20
    table-concurrency = 20
    view-concurrency = 10
  }
  read-update-tree-dataset {
    read-write-ratio = 0.8
    throughput = 50
    duration-in-minutes = 5
  }
}
```

**Make target:** `make minio-create-medium`

### High Load (Production Testing)

```hocon
dataset.tree {
  namespace-width = 4
  namespace-depth = 4
  tables-per-namespace = 20
  views-per-namespace = 10
}

workload {
  create-tree-dataset {
    table-concurrency = 50
    view-concurrency = 25
  }
  read-tree-dataset {
    namespace-concurrency = 50
    table-concurrency = 50
    view-concurrency = 25
  }
  read-update-tree-dataset {
    read-write-ratio = 0.7
    throughput = 200
    duration-in-minutes = 10
  }
  create-commits {
    table-commits-throughput = 50
    view-commits-throughput = 25
    duration-in-minutes = 5
  }
}
```

**Make target:** `make minio-create-high`

### Stress Test (Maximum Load)

```hocon
dataset.tree {
  namespace-width = 5
  namespace-depth = 5
  tables-per-namespace = 50
  views-per-namespace = 25
}

workload {
  create-tree-dataset {
    table-concurrency = 100
    view-concurrency = 50
  }
  read-tree-dataset {
    namespace-concurrency = 100
    table-concurrency = 100
    view-concurrency = 50
  }
  read-update-tree-dataset {
    read-write-ratio = 0.5
    throughput = 500
    duration-in-minutes = 15
  }
  create-commits {
    table-commits-throughput = 100
    view-commits-throughput = 50
    duration-in-minutes = 10
  }
}
```

**Make target:** `make minio-stress`

## Full Benchmark Workflow

Run a complete benchmark cycle:

```bash
# 1. Create the dataset
make minio-create

# 2. Run read tests
make minio-read

# 3. Run mixed read/write tests
make minio-read-update

# 4. Run commit tests
make minio-commits

# 5. Run weighted workload
make minio-weighted

# 6. View reports
make reports-list
```

Or run all in sequence:

```bash
make minio-full-benchmark
```

## Configuration Reference

### HTTP Settings

| Parameter | Description | Default |
|-----------|-------------|---------|
| `http.base-url` | MinIO endpoint URL | `http://localhost:9001/_iceberg` |
| `http.api-prefix` | API path prefix | `/v1` |

### SIGV4 Authentication

| Parameter | Description | Default |
|-----------|-------------|---------|
| `auth.type` | Auth type (`sigv4` for MinIO) | `oauth2` |
| `auth.sigv4.access-key` | AWS/MinIO access key | - |
| `auth.sigv4.secret-key` | AWS/MinIO secret key | - |
| `auth.sigv4.region` | AWS region | `us-east-1` |
| `auth.sigv4.service` | Service name for signing | `s3tables` |

### Dataset Parameters

| Parameter | Description | Default |
|-----------|-------------|---------|
| `dataset.tree.skip-catalog-creation` | Use existing catalog | `false` |
| `dataset.tree.catalog-name` | Catalog/bucket name | `C_0` |
| `dataset.tree.namespace-width` | Children per namespace | `2` |
| `dataset.tree.namespace-depth` | Namespace tree depth | `4` |
| `dataset.tree.tables-per-namespace` | Tables per leaf namespace | `5` |
| `dataset.tree.views-per-namespace` | Views per leaf namespace | `3` |

### Dataset Size Examples

| Width | Depth | Leaf NS | Tables/NS | Total Namespaces | Total Tables |
|-------|-------|---------|-----------|------------------|--------------|
| 2 | 1 | 1 | 2 | 1 | 2 |
| 2 | 2 | 2 | 5 | 3 | 10 |
| 3 | 3 | 9 | 10 | 13 | 90 |
| 4 | 4 | 64 | 20 | 85 | 1,280 |
| 5 | 5 | 625 | 50 | 781 | 31,250 |

## Viewing Reports

```bash
# List all reports
make reports-list

# Clean old reports
make reports-clean
```

Reports are generated in `build/reports/gatling/` with an HTML index file.

## Troubleshooting

### Java Version Issues

If you see errors like `25.0.1` during build, you're using Java 25. Switch to Java 21:

```bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk
```

### 403 Signature Errors

- Verify access key and secret key are correct
- Check region and service name match your MinIO configuration
- Ensure the bucket/catalog exists

### 409 Conflict Errors

This is expected when running benchmarks multiple times - namespaces/tables already exist.
The benchmark treats 409 as success (idempotent create).

### Connection Refused

- Verify MinIO is running: `curl http://localhost:9001/_iceberg/v1/config`
- Check the `http.base-url` in your configuration
