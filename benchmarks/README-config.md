# Benchmark Configuration Reference

This document explains every configuration parameter, formulas for dataset sizing, and how they affect benchmark behavior.

## Configuration File Loading

The benchmark uses [Typesafe Config](https://github.com/lightbend/config) (HOCON format) with the following priority:

1. **System properties** (`-Dkey=value`) - Highest priority
2. **application.conf** - Your custom config
3. **benchmark-defaults.conf** - Default values

Example override chain:
```bash
# benchmark-defaults.conf has: namespace-width = 2
# application.conf has: namespace-width = 3
# Command line: -Ddataset.tree.namespace-width=4

# Result: namespace-width = 4 (command line wins)
```

## HTTP Settings

```hocon
http {
  base-url = "http://localhost:8181"
  api-prefix = "/api/catalog/v1"
}
```

| Parameter | Description | Examples |
|-----------|-------------|----------|
| `base-url` | Base URL of the Iceberg REST catalog server | `http://localhost:8181`, `http://minio:9001/_iceberg` |
| `api-prefix` | Path prefix for Iceberg REST API endpoints | `/api/catalog/v1` (Polaris), `/v1` (MinIO), `/catalog/v1` (Lakekeeper) |

**How it's used:** Every API call is made to `{base-url}{api-prefix}/{endpoint}`

Example: `http://localhost:9001/_iceberg` + `/v1` + `/testw/namespaces` = `http://localhost:9001/_iceberg/v1/testw/namespaces`

## Authentication Settings

### OAuth2 (Default - for Polaris)

```hocon
auth {
  type = "oauth2"
  client-id = "my-client-id"
  client-secret = "my-client-secret"
  refresh-interval-seconds = 60
  max-retries = 10
  retryable-http-codes = [500]
}
```

| Parameter | Description | Default |
|-----------|-------------|---------|
| `type` | Authentication type | `oauth2` |
| `client-id` | OAuth2 client ID | Required |
| `client-secret` | OAuth2 client secret | Required |
| `refresh-interval-seconds` | How often to refresh the access token | `60` |
| `max-retries` | Max retry attempts for auth failures | `10` |
| `retryable-http-codes` | HTTP codes that trigger retry | `[500]` |

### SIGV4 (for MinIO, AWS S3 Tables)

```hocon
auth {
  type = "sigv4"
  sigv4 {
    access-key = "minioadmin"
    secret-key = "minioadmin"
    region = "us-east-1"
    service = "s3tables"
  }
}
```

| Parameter | Description | Default |
|-----------|-------------|---------|
| `type` | Authentication type | `sigv4` |
| `sigv4.access-key` | AWS/MinIO access key ID | Required |
| `sigv4.secret-key` | AWS/MinIO secret access key | Required |
| `sigv4.region` | AWS region for signing | `us-east-1` |
| `sigv4.service` | AWS service name for signing | `s3tables` |

**How SIGV4 works:** Each HTTP request is signed using AWS Signature Version 4 algorithm. The signature is computed from the request method, path, headers, body, timestamp, and your credentials.

## Dataset Tree Structure

The benchmark creates a hierarchical namespace structure (n-ary tree) containing tables and views.

```hocon
dataset.tree {
  skip-catalog-creation = false
  catalog-name = "C_0"
  storage-config-info = """{"storageType": "FILE"}"""
  num-catalogs = 1
  
  namespace-width = 2
  namespace-depth = 4
  
  tables-per-namespace = 5
  views-per-namespace = 3
  max-tables = -1
  max-views = -1
  
  columns-per-table = 10
  columns-per-view = 10
  
  default-base-location = "file:///tmp/polaris"
  
  namespace-properties = 10
  table-properties = 10
  view-properties = 10
  
  mangle-names = false
}
```

### Catalog Settings

| Parameter | Description | Default |
|-----------|-------------|---------|
| `skip-catalog-creation` | If `true`, use existing catalog. If `false`, create new catalog. | `false` |
| `catalog-name` | Name of catalog to use/create | `C_0` |
| `storage-config-info` | JSON for StorageConfigInfo when creating catalog | `{"storageType": "FILE"}` |
| `num-catalogs` | Number of catalogs to create (only first is used for dataset) | `1` |

**When to use `skip-catalog-creation = true`:**
- Testing against MinIO S3 Tables (catalog = bucket, already exists)
- Testing against any non-Polaris Iceberg REST implementation
- Testing against a pre-configured Polaris catalog

### Namespace Tree Parameters

| Parameter | Description | Default |
|-----------|-------------|---------|
| `namespace-width` | Number of children per namespace (N) | `2` |
| `namespace-depth` | Depth of namespace tree including root (D) | `4` |

#### Tree Structure Formula

```
Total Namespaces = (N^D - 1) / (N - 1)    [for N > 1]
Total Namespaces = D                       [for N = 1]

Leaf Namespaces = N^(D-1)
```

**Visual example** (width=2, depth=3):
```
Level 0 (root):     NS_0
                   /    \
Level 1:        NS_1    NS_2
               /   \    /   \
Level 2:    NS_3  NS_4 NS_5  NS_6  <-- Leaf namespaces (tables/views go here)
```

- Total namespaces: (2^3 - 1) / (2 - 1) = 7
- Leaf namespaces: 2^(3-1) = 4

#### Namespace Count Examples

| Width (N) | Depth (D) | Total Namespaces | Leaf Namespaces |
|-----------|-----------|------------------|-----------------|
| 2 | 1 | 1 | 1 |
| 2 | 2 | 3 | 2 |
| 2 | 3 | 7 | 4 |
| 2 | 4 | 15 | 8 |
| 3 | 3 | 13 | 9 |
| 3 | 4 | 40 | 27 |
| 4 | 4 | 85 | 64 |
| 5 | 5 | 781 | 625 |

### Table and View Parameters

| Parameter | Description | Default |
|-----------|-------------|---------|
| `tables-per-namespace` | Tables created in each **leaf** namespace | `5` |
| `views-per-namespace` | Views created in each **leaf** namespace | `3` |
| `max-tables` | Cap on total tables (-1 = no cap) | `-1` |
| `max-views` | Cap on total views (-1 = no cap) | `-1` |
| `columns-per-table` | Number of columns in each table schema | `10` |
| `columns-per-view` | Number of columns in each view schema | `10` |

#### Table/View Count Formula

```
Max Possible Tables = Leaf_Namespaces × tables-per-namespace
Max Possible Tables = N^(D-1) × T

Max Possible Views = Leaf_Namespaces × views-per-namespace  
Max Possible Views = N^(D-1) × V

Actual Tables = min(max-tables, Max_Possible_Tables)  [if max-tables > 0]
Actual Tables = Max_Possible_Tables                   [if max-tables = -1]
```

#### Dataset Size Examples

| Width | Depth | Tables/NS | Views/NS | Leaf NS | Total Tables | Total Views |
|-------|-------|-----------|----------|---------|--------------|-------------|
| 2 | 1 | 2 | 1 | 1 | 2 | 1 |
| 2 | 2 | 5 | 3 | 2 | 10 | 6 |
| 2 | 3 | 5 | 3 | 4 | 20 | 12 |
| 2 | 4 | 5 | 3 | 8 | 40 | 24 |
| 3 | 3 | 10 | 5 | 9 | 90 | 45 |
| 4 | 4 | 20 | 10 | 64 | 1,280 | 640 |
| 5 | 5 | 50 | 25 | 625 | 31,250 | 15,625 |

#### Using max-tables to Cap Dataset Size

```hocon
# Without cap: 2^19 * 4 = 2,097,152 tables
dataset.tree {
  namespace-width = 2
  namespace-depth = 20
  tables-per-namespace = 4
  max-tables = -1
}

# With cap: exactly 1,000,000 tables
dataset.tree {
  namespace-width = 2
  namespace-depth = 20
  tables-per-namespace = 4
  max-tables = 1000000
}
```

### Property Settings

| Parameter | Description | Default |
|-----------|-------------|---------|
| `namespace-properties` | Number of properties added to each namespace | `10` |
| `table-properties` | Number of properties added to each table | `10` |
| `view-properties` | Number of properties added to each view | `10` |

Properties are named `InitialAttribute_0`, `InitialAttribute_1`, etc.

**Note for MinIO:** MinIO S3 Tables requires lowercase property keys. Set these to `0` to avoid issues:
```hocon
namespace-properties = 0
table-properties = 0
view-properties = 0
```

### Other Dataset Settings

| Parameter | Description | Default |
|-----------|-------------|---------|
| `default-base-location` | Base storage location for tables/views | `file:///tmp/polaris` |
| `mangle-names` | Replace entity names with MD5 hashes | `false` |

**Location formula:**
```
Table location: {default-base-location}/{catalog}/{namespace-path}/{table-name}
Example: s3://mybucket/C_0/NS_0/NS_1/T_0
```

## Workload Settings

### CreateTreeDataset Simulation

Creates namespaces, tables, and views.

```hocon
workload.create-tree-dataset {
  table-concurrency = 20
  view-concurrency = 10
}
```

| Parameter | Description | Default |
|-----------|-------------|---------|
| `table-concurrency` | Concurrent table creation operations | `20` |
| `view-concurrency` | Concurrent view creation operations | `10` |

**Behavior:**
1. Creates namespaces sequentially (parent must exist before child)
2. Creates tables with `table-concurrency` parallel workers
3. Creates views with `view-concurrency` parallel workers

### ReadTreeDataset Simulation

Reads existing namespaces, tables, and views.

```hocon
workload.read-tree-dataset {
  namespace-concurrency = 20
  table-concurrency = 20
  view-concurrency = 10
}
```

| Parameter | Description | Default |
|-----------|-------------|---------|
| `namespace-concurrency` | Concurrent namespace read operations | `20` |
| `table-concurrency` | Concurrent table read operations | `20` |
| `view-concurrency` | Concurrent view read operations | `10` |

### ReadUpdateTreeDataset Simulation

Mixed read/write workload on existing dataset.

```hocon
workload.read-update-tree-dataset {
  read-write-ratio = 0.5
  throughput = 100
  duration-in-minutes = 5
}
```

| Parameter | Description | Default |
|-----------|-------------|---------|
| `read-write-ratio` | Ratio of reads to total ops (0.0 = all writes, 1.0 = all reads) | `0.5` |
| `throughput` | Operations per second | `100` |
| `duration-in-minutes` | How long to run | `5` |

**Examples:**
- `read-write-ratio = 0.8, throughput = 100` → 80 reads/sec, 20 writes/sec
- `read-write-ratio = 0.5, throughput = 50` → 25 reads/sec, 25 writes/sec

### CreateCommits Simulation

Creates commits (updates) on existing tables and views.

```hocon
workload.create-commits {
  table-commits-throughput = 10
  view-commits-throughput = 5
  duration-in-minutes = 1
}
```

| Parameter | Description | Default |
|-----------|-------------|---------|
| `table-commits-throughput` | Table commits per second | `10` |
| `view-commits-throughput` | View commits per second | `5` |
| `duration-in-minutes` | How long to run | `1` |

### WeightedWorkloadOnTreeDataset Simulation

Advanced workload with configurable reader/writer distributions.

```hocon
workload.weighted-workload-on-tree-dataset {
  seed = 42
  readers = [
    { count = 8, mean = 0.3, variance = 0.0278 }
  ]
  writers = [
    { count = 2, mean = 0.7, variance = 0.0278 }
  ]
  duration-in-minutes = 5
}
```

| Parameter | Description | Default |
|-----------|-------------|---------|
| `seed` | Random seed for reproducibility | `42` |
| `readers` | List of reader thread configurations | See below |
| `writers` | List of writer thread configurations | See below |
| `duration-in-minutes` | How long to run | `5` |

**Reader/Writer distribution:**
- `count`: Number of threads with this distribution
- `mean`: Center of normal distribution (0.0 to 1.0 across table space)
- `variance`: Spread of the distribution

**Example interpretation:**
- Readers with `mean=0.3, variance=0.0278` focus on tables in the first third of the table space
- Writers with `mean=0.7, variance=0.0278` focus on tables in the last third
- This simulates hot spots in different parts of the catalog

## Complete Configuration Examples

### Light Load (Development)

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
  catalog-name = "test"
  default-base-location = "s3://test"
  
  namespace-width = 2
  namespace-depth = 1
  tables-per-namespace = 2
  views-per-namespace = 1
  
  namespace-properties = 0
  table-properties = 0
  view-properties = 0
}

workload {
  create-tree-dataset {
    table-concurrency = 5
    view-concurrency = 2
  }
}
```

**Result:** 1 namespace, 2 tables, 1 view

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
    read-write-ratio = 0.7
    throughput = 50
    duration-in-minutes = 5
  }
}
```

**Result:** 13 namespaces, 90 tables, 45 views

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
    read-write-ratio = 0.6
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

**Result:** 85 namespaces, 1,280 tables, 640 views

### Stress Test

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
  read-update-tree-dataset {
    read-write-ratio = 0.5
    throughput = 500
    duration-in-minutes = 15
  }
}
```

**Result:** 781 namespaces, 31,250 tables, 15,625 views

## Quick Reference Card

### Dataset Size Formula
```
Leaf Namespaces    = width^(depth-1)
Total Namespaces   = (width^depth - 1) / (width - 1)
Total Tables       = width^(depth-1) × tables-per-namespace
Total Views        = width^(depth-1) × views-per-namespace
```

### Common Configurations

| Use Case | Width | Depth | Tables/NS | Total Tables |
|----------|-------|-------|-----------|--------------|
| Quick test | 2 | 1 | 2 | 2 |
| Development | 2 | 2 | 5 | 10 |
| Integration | 3 | 3 | 10 | 90 |
| Performance | 4 | 4 | 20 | 1,280 |
| Stress | 5 | 5 | 50 | 31,250 |
| Large scale | 2 | 20 | 4 | 2,097,152 |
