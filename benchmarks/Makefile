#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
#

.DEFAULT_GOAL := help

.PHONY: help
help:
	@echo "Iceberg REST Catalog Benchmark Commands"
	@echo "========================================"
	@echo ""
	@echo "Polaris (OAuth2) Simulations:"
	@echo "  make create-dataset-simulation  - Create namespaces, tables, views"
	@echo "  make read-simulation            - Read existing dataset"
	@echo "  make read-update-simulation     - Mixed read/write workload"
	@echo "  make create-commits-simulation  - Create commits on tables/views"
	@echo "  make weighted-workload-simulation - Weighted workload"
	@echo ""
	@echo "MinIO S3 Tables (SIGV4) Simulations: (uses application.conf settings)"
	@echo "  make minio-help                 - Show all MinIO commands"
	@echo "  make minio-create               - Create namespaces, tables, views"
	@echo "  make minio-read                 - Read existing dataset"
	@echo "  make minio-read-update          - Mixed read/write workload"
	@echo "  make minio-commits              - Create commits on tables/views"
	@echo "  make minio-weighted             - Weighted workload"
	@echo ""
	@echo "MinIO Load Profiles (Create Dataset):"
	@echo "  make minio-create-light    - 2 tables, 1 view, 5 concurrency"
	@echo "  make minio-create-medium   - 90 tables, 45 views, 20 concurrency"
	@echo "  make minio-create-high     - 1,280 tables, 640 views, 50 concurrency"
	@echo "  make minio-stress          - 31K tables, 15K views, 100 concurrency"
	@echo ""
	@echo "MinIO Load Profiles (Read Dataset): [REQUIRES: minio-create-* first]"
	@echo "  make minio-read-light      - 5 concurrent readers"
	@echo "  make minio-read-medium     - 20 concurrent readers"
	@echo "  make minio-read-high       - 50 concurrent readers"
	@echo "  make minio-read-stress     - 100 concurrent readers"
	@echo ""
	@echo "MinIO Load Profiles (Mixed Read/Write): [REQUIRES: minio-create-* first]"
	@echo "  make minio-mixed-light     - 20 ops/sec, 80%% reads, 2 min"
	@echo "  make minio-mixed-medium    - 50 ops/sec, 70%% reads, 5 min"
	@echo "  make minio-mixed-high      - 200 ops/sec, 60%% reads, 10 min"
	@echo "  make minio-mixed-stress    - 500 ops/sec, 50%% reads, 15 min"
	@echo ""
	@echo "MinIO Load Profiles (Commits): [REQUIRES: minio-create-* first]"
	@echo "  make minio-commits-light   - 7 commits/sec, 1 min"
	@echo "  make minio-commits-medium  - 30 commits/sec, 3 min"
	@echo "  make minio-commits-high    - 75 commits/sec, 5 min"
	@echo "  make minio-commits-stress  - 150 commits/sec, 10 min"
	@echo ""
	@echo "MinIO Load Profiles (Weighted Workload): [REQUIRES: minio-create-* first]"
	@echo "  make minio-weighted-light  - 2 min duration"
	@echo "  make minio-weighted-medium - 5 min duration"
	@echo "  make minio-weighted-high   - 10 min duration"
	@echo "  make minio-weighted-stress - 30 min duration"
	@echo ""
	@echo "Full Benchmark Suites:"
	@echo "  make minio-full-benchmark         - Run all base simulations"
	@echo "  make minio-full-benchmark-light   - All light load simulations"
	@echo "  make minio-full-benchmark-medium  - All medium load simulations"
	@echo "  make minio-full-benchmark-high    - All high load simulations"
	@echo "  make minio-full-benchmark-stress  - All stress test simulations"
	@echo ""
	@echo "Custom Parameters (pass your own values):"
	@echo "  make minio-create-custom   NS_WIDTH=4 NS_DEPTH=4 TABLES=20 VIEWS=10 CREATE_CONCURRENCY=50"
	@echo "                             NS_WIDTH: children per namespace, NS_DEPTH: tree depth"
	@echo "                             TABLES/VIEWS: per leaf namespace, CREATE_CONCURRENCY: parallel ops"
	@echo "  make minio-read-custom     READ_CONCURRENCY=100"
	@echo "                             READ_CONCURRENCY: number of parallel read operations"
	@echo "  make minio-mixed-custom    THROUGHPUT=1000 READ_RATIO=0.8 DURATION=10"
	@echo "                             THROUGHPUT: ops/sec, READ_RATIO: 0.8=80%% reads, DURATION: minutes"
	@echo "  make minio-commits-custom  TABLE_COMMITS=500 VIEW_COMMITS=200 DURATION=5"
	@echo "                             TABLE_COMMITS/VIEW_COMMITS: commits/sec, DURATION: minutes"
	@echo "  make minio-weighted-custom DURATION=30"
	@echo "                             DURATION: test duration in minutes"
	@echo ""
	@echo "Enterprise Scale:"
	@echo "  make minio-commits-enterprise     - 3K commits/sec, 10 min"
	@echo "  make minio-mixed-enterprise       - 10K ops/sec, 15 min"
	@echo "  make minio-commits-extreme        - 15K commits/sec, 15 min"
	@echo "  make minio-mixed-extreme          - 50K ops/sec, 20 min"
	@echo ""
	@echo "Reports:"
	@echo "  make reports-list               - List all generated reports"
	@echo "  make reports-clean              - Delete all reports"
	@echo ""
	@echo "Prerequisites:"
	@echo "  export JAVA_HOME=/usr/lib/jvm/java-21-openjdk"
	@echo "  Create application.conf with your credentials"
	@echo ""
	@echo "Run 'make minio-help' for all MinIO commands and options."

.PHONY: create-dataset-simulation
create-dataset-simulation:
	./gradlew gatlingRun --simulation org.apache.polaris.benchmarks.simulations.CreateTreeDataset \
      -Dconfig.file=./application.conf

.PHONY: read-simulation
read-simulation:
	./gradlew gatlingRun --simulation org.apache.polaris.benchmarks.simulations.ReadTreeDataset \
      -Dconfig.file=./application.conf

.PHONY: read-update-simulation
read-update-simulation:
	./gradlew gatlingRun --simulation org.apache.polaris.benchmarks.simulations.ReadUpdateTreeDataset \
      -Dconfig.file=./application.conf

.PHONY: create-commits-simulation
create-commits-simulation:
	./gradlew gatlingRun --simulation org.apache.polaris.benchmarks.simulations.CreateCommits \
      -Dconfig.file=./application.conf

.PHONY: weighted-workload-simulation
weighted-workload-simulation:
	./gradlew gatlingRun --simulation org.apache.polaris.benchmarks.simulations.WeightedWorkloadOnTreeDataset \
      -Dconfig.file=./application.conf

.PHONY: reports-list
reports-list:
	@for report in $$(ls -d build/reports/gatling/*/ 2>/dev/null | sort -r); do \
		basename="$$(basename $$report)"; \
		name="$$(echo $$basename | sed 's/-[0-9]\{17\}$$//')"; \
		timestamp="$$(echo $$basename | grep -o '[0-9]\{17\}$$')"; \
		if [ -n "$$timestamp" ]; then \
			date="$$(echo $$timestamp | cut -c1-8)"; \
			time="$$(echo $$timestamp | cut -c9-14)"; \
			millis="$$(echo $$timestamp | cut -c15-17)"; \
			formatted_date="$$(echo $$date | sed 's/\([0-9]\{4\}\)\([0-9]\{2\}\)\([0-9]\{2\}\)/\1-\2-\3/')"; \
			formatted_time="$$(echo $$time | sed 's/\([0-9]\{2\}\)\([0-9]\{2\}\)\([0-9]\{2\}\)/\1:\2:\3/')"; \
			index_path="$${report}index.html"; \
			if [ -f "$$index_path" ]; then \
				echo "$$name | $$formatted_date $$formatted_time.$$millis | $$index_path"; \
			else \
				echo "$$name | $$formatted_date $$formatted_time.$$millis | ERROR"; \
			fi; \
		fi; \
	done

.PHONY: reports-clean
reports-clean:
	@rm -rf build/reports/gatling/*/ 2>/dev/null || true

# =============================================================================
# MinIO S3 Tables Benchmarks (SIGV4 Authentication)
# =============================================================================
# These targets run benchmarks against MinIO S3 Tables using SIGV4 auth.
# Configuration is loaded from application.conf in the benchmarks directory.
#
# Prerequisites:
#   - Java 21 (export JAVA_HOME=/usr/lib/jvm/java-21-openjdk)
#   - MinIO with S3 Tables enabled
#   - application.conf with SIGV4 credentials and catalog name
# =============================================================================

MINIO_CONFIG ?= ./application.conf
GRADLE_OPTS ?=

# =============================================================================
# Parameterized Benchmark Targets
# =============================================================================
# Pass custom values to any simulation. Examples:
#   make minio-create-custom NS_WIDTH=4 NS_DEPTH=4 TABLES=20 VIEWS=10 CONCURRENCY=50
#   make minio-read-custom CONCURRENCY=100
#   make minio-mixed-custom THROUGHPUT=1000 READ_RATIO=0.8 DURATION=10
#   make minio-commits-custom TABLE_COMMITS=500 VIEW_COMMITS=200 DURATION=5
#   make minio-weighted-custom DURATION=30
# =============================================================================

# Create dataset parameters
NS_WIDTH ?= 2
NS_DEPTH ?= 2
TABLES ?= 5
VIEWS ?= 3
CREATE_CONCURRENCY ?= 20
VIEW_CONCURRENCY ?= 10

# Read parameters
READ_CONCURRENCY ?= 20

# Mixed read/write parameters
THROUGHPUT ?= 100
READ_RATIO ?= 0.5
DURATION ?= 5

# Commit parameters
TABLE_COMMITS ?= 10
VIEW_COMMITS ?= 5

# Weighted parameters (uses same DURATION)

.PHONY: minio-create-custom
minio-create-custom:
	@echo "Creating dataset: width=$(NS_WIDTH), depth=$(NS_DEPTH), tables/ns=$(TABLES), views/ns=$(VIEWS), concurrency=$(CREATE_CONCURRENCY)"
	./gradlew gatlingRun --simulation org.apache.polaris.benchmarks.simulations.CreateTreeDataset \
		-Dconfig.file=$(MINIO_CONFIG) \
		-Ddataset.tree.namespace-width=$(NS_WIDTH) \
		-Ddataset.tree.namespace-depth=$(NS_DEPTH) \
		-Ddataset.tree.tables-per-namespace=$(TABLES) \
		-Ddataset.tree.views-per-namespace=$(VIEWS) \
		-Dworkload.create-tree-dataset.table-concurrency=$(CREATE_CONCURRENCY) \
		-Dworkload.create-tree-dataset.view-concurrency=$(VIEW_CONCURRENCY) $(GRADLE_OPTS)

.PHONY: minio-read-custom
minio-read-custom:
	@echo "Reading dataset: concurrency=$(READ_CONCURRENCY)"
	./gradlew gatlingRun --simulation org.apache.polaris.benchmarks.simulations.ReadTreeDataset \
		-Dconfig.file=$(MINIO_CONFIG) \
		-Dworkload.read-tree-dataset.namespace-concurrency=$(READ_CONCURRENCY) \
		-Dworkload.read-tree-dataset.table-concurrency=$(READ_CONCURRENCY) \
		-Dworkload.read-tree-dataset.view-concurrency=$(READ_CONCURRENCY) $(GRADLE_OPTS)

.PHONY: minio-mixed-custom
minio-mixed-custom:
	@echo "Mixed workload: throughput=$(THROUGHPUT) ops/sec, read_ratio=$(READ_RATIO), duration=$(DURATION) min"
	./gradlew gatlingRun --simulation org.apache.polaris.benchmarks.simulations.ReadUpdateTreeDataset \
		-Dconfig.file=$(MINIO_CONFIG) \
		-Dworkload.read-update-tree-dataset.throughput=$(THROUGHPUT) \
		-Dworkload.read-update-tree-dataset.read-write-ratio=$(READ_RATIO) \
		-Dworkload.read-update-tree-dataset.duration-in-minutes=$(DURATION) $(GRADLE_OPTS)

.PHONY: minio-commits-custom
minio-commits-custom:
	@echo "Commits: table=$(TABLE_COMMITS)/sec, view=$(VIEW_COMMITS)/sec, duration=$(DURATION) min"
	./gradlew gatlingRun --simulation org.apache.polaris.benchmarks.simulations.CreateCommits \
		-Dconfig.file=$(MINIO_CONFIG) \
		-Dworkload.create-commits.table-commits-throughput=$(TABLE_COMMITS) \
		-Dworkload.create-commits.view-commits-throughput=$(VIEW_COMMITS) \
		-Dworkload.create-commits.duration-in-minutes=$(DURATION) $(GRADLE_OPTS)

.PHONY: minio-weighted-custom
minio-weighted-custom:
	@echo "Weighted workload: duration=$(DURATION) min"
	./gradlew gatlingRun --simulation org.apache.polaris.benchmarks.simulations.WeightedWorkloadOnTreeDataset \
		-Dconfig.file=$(MINIO_CONFIG) \
		-Dworkload.weighted-workload-on-tree-dataset.duration-in-minutes=$(DURATION) $(GRADLE_OPTS)

# =============================================================================
# Enterprise-Scale Load Profiles
# =============================================================================
# Realistic production-level benchmarks for enterprise systems
# =============================================================================

.PHONY: minio-commits-enterprise
minio-commits-enterprise:
	@echo "Enterprise commits: 2000 table + 1000 view commits/sec for 10 min"
	./gradlew gatlingRun --simulation org.apache.polaris.benchmarks.simulations.CreateCommits \
		-Dconfig.file=$(MINIO_CONFIG) \
		-Dworkload.create-commits.table-commits-throughput=2000 \
		-Dworkload.create-commits.view-commits-throughput=1000 \
		-Dworkload.create-commits.duration-in-minutes=10 $(GRADLE_OPTS)

.PHONY: minio-mixed-enterprise
minio-mixed-enterprise:
	@echo "Enterprise mixed: 10000 ops/sec, 70% reads for 15 min"
	./gradlew gatlingRun --simulation org.apache.polaris.benchmarks.simulations.ReadUpdateTreeDataset \
		-Dconfig.file=$(MINIO_CONFIG) \
		-Dworkload.read-update-tree-dataset.throughput=10000 \
		-Dworkload.read-update-tree-dataset.read-write-ratio=0.7 \
		-Dworkload.read-update-tree-dataset.duration-in-minutes=15 $(GRADLE_OPTS)

.PHONY: minio-read-enterprise
minio-read-enterprise:
	@echo "Enterprise read: 500 concurrent readers"
	./gradlew gatlingRun --simulation org.apache.polaris.benchmarks.simulations.ReadTreeDataset \
		-Dconfig.file=$(MINIO_CONFIG) \
		-Dworkload.read-tree-dataset.namespace-concurrency=500 \
		-Dworkload.read-tree-dataset.table-concurrency=500 \
		-Dworkload.read-tree-dataset.view-concurrency=250 $(GRADLE_OPTS)

.PHONY: minio-commits-extreme
minio-commits-extreme:
	@echo "Extreme commits: 10000 table + 5000 view commits/sec for 15 min"
	./gradlew gatlingRun --simulation org.apache.polaris.benchmarks.simulations.CreateCommits \
		-Dconfig.file=$(MINIO_CONFIG) \
		-Dworkload.create-commits.table-commits-throughput=10000 \
		-Dworkload.create-commits.view-commits-throughput=5000 \
		-Dworkload.create-commits.duration-in-minutes=15 $(GRADLE_OPTS)

.PHONY: minio-mixed-extreme
minio-mixed-extreme:
	@echo "Extreme mixed: 50000 ops/sec, 60% reads for 20 min"
	./gradlew gatlingRun --simulation org.apache.polaris.benchmarks.simulations.ReadUpdateTreeDataset \
		-Dconfig.file=$(MINIO_CONFIG) \
		-Dworkload.read-update-tree-dataset.throughput=50000 \
		-Dworkload.read-update-tree-dataset.read-write-ratio=0.6 \
		-Dworkload.read-update-tree-dataset.duration-in-minutes=20 $(GRADLE_OPTS)

# Base simulations for MinIO
.PHONY: minio-create
minio-create:
	./gradlew gatlingRun --simulation org.apache.polaris.benchmarks.simulations.CreateTreeDataset \
		-Dconfig.file=$(MINIO_CONFIG) $(GRADLE_OPTS)

.PHONY: minio-read
minio-read:
	./gradlew gatlingRun --simulation org.apache.polaris.benchmarks.simulations.ReadTreeDataset \
		-Dconfig.file=$(MINIO_CONFIG) $(GRADLE_OPTS)

.PHONY: minio-read-update
minio-read-update:
	./gradlew gatlingRun --simulation org.apache.polaris.benchmarks.simulations.ReadUpdateTreeDataset \
		-Dconfig.file=$(MINIO_CONFIG) $(GRADLE_OPTS)

.PHONY: minio-commits
minio-commits:
	./gradlew gatlingRun --simulation org.apache.polaris.benchmarks.simulations.CreateCommits \
		-Dconfig.file=$(MINIO_CONFIG) $(GRADLE_OPTS)

.PHONY: minio-weighted
minio-weighted:
	./gradlew gatlingRun --simulation org.apache.polaris.benchmarks.simulations.WeightedWorkloadOnTreeDataset \
		-Dconfig.file=$(MINIO_CONFIG) $(GRADLE_OPTS)

# Load profile configurations
.PHONY: minio-create-light
minio-create-light:
	./gradlew gatlingRun --simulation org.apache.polaris.benchmarks.simulations.CreateTreeDataset \
		-Dconfig.file=$(MINIO_CONFIG) \
		-Ddataset.tree.namespace-width=2 \
		-Ddataset.tree.namespace-depth=1 \
		-Ddataset.tree.tables-per-namespace=2 \
		-Ddataset.tree.views-per-namespace=1 \
		-Dworkload.create-tree-dataset.table-concurrency=5 \
		-Dworkload.create-tree-dataset.view-concurrency=2 $(GRADLE_OPTS)

.PHONY: minio-create-medium
minio-create-medium:
	./gradlew gatlingRun --simulation org.apache.polaris.benchmarks.simulations.CreateTreeDataset \
		-Dconfig.file=$(MINIO_CONFIG) \
		-Ddataset.tree.namespace-width=3 \
		-Ddataset.tree.namespace-depth=3 \
		-Ddataset.tree.tables-per-namespace=10 \
		-Ddataset.tree.views-per-namespace=5 \
		-Dworkload.create-tree-dataset.table-concurrency=20 \
		-Dworkload.create-tree-dataset.view-concurrency=10 $(GRADLE_OPTS)

.PHONY: minio-create-high
minio-create-high:
	./gradlew gatlingRun --simulation org.apache.polaris.benchmarks.simulations.CreateTreeDataset \
		-Dconfig.file=$(MINIO_CONFIG) \
		-Ddataset.tree.namespace-width=4 \
		-Ddataset.tree.namespace-depth=4 \
		-Ddataset.tree.tables-per-namespace=20 \
		-Ddataset.tree.views-per-namespace=10 \
		-Dworkload.create-tree-dataset.table-concurrency=50 \
		-Dworkload.create-tree-dataset.view-concurrency=25 $(GRADLE_OPTS)

.PHONY: minio-stress
minio-stress:
	./gradlew gatlingRun --simulation org.apache.polaris.benchmarks.simulations.CreateTreeDataset \
		-Dconfig.file=$(MINIO_CONFIG) \
		-Ddataset.tree.namespace-width=5 \
		-Ddataset.tree.namespace-depth=5 \
		-Ddataset.tree.tables-per-namespace=50 \
		-Ddataset.tree.views-per-namespace=25 \
		-Dworkload.create-tree-dataset.table-concurrency=100 \
		-Dworkload.create-tree-dataset.view-concurrency=50 $(GRADLE_OPTS)

# Read workload profiles
.PHONY: minio-read-light
minio-read-light:
	./gradlew gatlingRun --simulation org.apache.polaris.benchmarks.simulations.ReadTreeDataset \
		-Dconfig.file=$(MINIO_CONFIG) \
		-Dworkload.read-tree-dataset.namespace-concurrency=5 \
		-Dworkload.read-tree-dataset.table-concurrency=5 \
		-Dworkload.read-tree-dataset.view-concurrency=2 $(GRADLE_OPTS)

.PHONY: minio-read-medium
minio-read-medium:
	./gradlew gatlingRun --simulation org.apache.polaris.benchmarks.simulations.ReadTreeDataset \
		-Dconfig.file=$(MINIO_CONFIG) \
		-Ddataset.tree.namespace-width=3 \
		-Ddataset.tree.namespace-depth=3 \
		-Ddataset.tree.tables-per-namespace=10 \
		-Ddataset.tree.views-per-namespace=5 \
		-Dworkload.read-tree-dataset.namespace-concurrency=20 \
		-Dworkload.read-tree-dataset.table-concurrency=20 \
		-Dworkload.read-tree-dataset.view-concurrency=10 $(GRADLE_OPTS)

.PHONY: minio-read-high
minio-read-high:
	./gradlew gatlingRun --simulation org.apache.polaris.benchmarks.simulations.ReadTreeDataset \
		-Dconfig.file=$(MINIO_CONFIG) \
		-Ddataset.tree.namespace-width=4 \
		-Ddataset.tree.namespace-depth=4 \
		-Ddataset.tree.tables-per-namespace=20 \
		-Ddataset.tree.views-per-namespace=10 \
		-Dworkload.read-tree-dataset.namespace-concurrency=50 \
		-Dworkload.read-tree-dataset.table-concurrency=50 \
		-Dworkload.read-tree-dataset.view-concurrency=25 $(GRADLE_OPTS)

.PHONY: minio-read-stress
minio-read-stress:
	./gradlew gatlingRun --simulation org.apache.polaris.benchmarks.simulations.ReadTreeDataset \
		-Dconfig.file=$(MINIO_CONFIG) \
		-Ddataset.tree.namespace-width=5 \
		-Ddataset.tree.namespace-depth=5 \
		-Ddataset.tree.tables-per-namespace=50 \
		-Ddataset.tree.views-per-namespace=25 \
		-Dworkload.read-tree-dataset.namespace-concurrency=100 \
		-Dworkload.read-tree-dataset.table-concurrency=100 \
		-Dworkload.read-tree-dataset.view-concurrency=50 $(GRADLE_OPTS)

# Mixed read/write workload profiles
.PHONY: minio-mixed-light
minio-mixed-light:
	./gradlew gatlingRun --simulation org.apache.polaris.benchmarks.simulations.ReadUpdateTreeDataset \
		-Dconfig.file=$(MINIO_CONFIG) \
		-Dworkload.read-update-tree-dataset.read-write-ratio=0.8 \
		-Dworkload.read-update-tree-dataset.throughput=20 \
		-Dworkload.read-update-tree-dataset.duration-in-minutes=2 $(GRADLE_OPTS)

.PHONY: minio-mixed-medium
minio-mixed-medium:
	./gradlew gatlingRun --simulation org.apache.polaris.benchmarks.simulations.ReadUpdateTreeDataset \
		-Dconfig.file=$(MINIO_CONFIG) \
		-Ddataset.tree.namespace-width=3 \
		-Ddataset.tree.namespace-depth=3 \
		-Ddataset.tree.tables-per-namespace=10 \
		-Ddataset.tree.views-per-namespace=5 \
		-Dworkload.read-update-tree-dataset.read-write-ratio=0.7 \
		-Dworkload.read-update-tree-dataset.throughput=50 \
		-Dworkload.read-update-tree-dataset.duration-in-minutes=5 $(GRADLE_OPTS)

.PHONY: minio-mixed-high
minio-mixed-high:
	./gradlew gatlingRun --simulation org.apache.polaris.benchmarks.simulations.ReadUpdateTreeDataset \
		-Dconfig.file=$(MINIO_CONFIG) \
		-Ddataset.tree.namespace-width=4 \
		-Ddataset.tree.namespace-depth=4 \
		-Ddataset.tree.tables-per-namespace=20 \
		-Ddataset.tree.views-per-namespace=10 \
		-Dworkload.read-update-tree-dataset.read-write-ratio=0.6 \
		-Dworkload.read-update-tree-dataset.throughput=200 \
		-Dworkload.read-update-tree-dataset.duration-in-minutes=10 $(GRADLE_OPTS)

.PHONY: minio-mixed-stress
minio-mixed-stress:
	./gradlew gatlingRun --simulation org.apache.polaris.benchmarks.simulations.ReadUpdateTreeDataset \
		-Dconfig.file=$(MINIO_CONFIG) \
		-Ddataset.tree.namespace-width=5 \
		-Ddataset.tree.namespace-depth=5 \
		-Ddataset.tree.tables-per-namespace=50 \
		-Ddataset.tree.views-per-namespace=25 \
		-Dworkload.read-update-tree-dataset.read-write-ratio=0.5 \
		-Dworkload.read-update-tree-dataset.throughput=500 \
		-Dworkload.read-update-tree-dataset.duration-in-minutes=15 $(GRADLE_OPTS)

# Commit workload profiles
.PHONY: minio-commits-light
minio-commits-light:
	./gradlew gatlingRun --simulation org.apache.polaris.benchmarks.simulations.CreateCommits \
		-Dconfig.file=$(MINIO_CONFIG) \
		-Dworkload.create-commits.table-commits-throughput=5 \
		-Dworkload.create-commits.view-commits-throughput=2 \
		-Dworkload.create-commits.duration-in-minutes=1 $(GRADLE_OPTS)

.PHONY: minio-commits-medium
minio-commits-medium:
	./gradlew gatlingRun --simulation org.apache.polaris.benchmarks.simulations.CreateCommits \
		-Dconfig.file=$(MINIO_CONFIG) \
		-Ddataset.tree.namespace-width=3 \
		-Ddataset.tree.namespace-depth=3 \
		-Ddataset.tree.tables-per-namespace=10 \
		-Ddataset.tree.views-per-namespace=5 \
		-Dworkload.create-commits.table-commits-throughput=20 \
		-Dworkload.create-commits.view-commits-throughput=10 \
		-Dworkload.create-commits.duration-in-minutes=3 $(GRADLE_OPTS)

.PHONY: minio-commits-high
minio-commits-high:
	./gradlew gatlingRun --simulation org.apache.polaris.benchmarks.simulations.CreateCommits \
		-Dconfig.file=$(MINIO_CONFIG) \
		-Ddataset.tree.namespace-width=4 \
		-Ddataset.tree.namespace-depth=4 \
		-Ddataset.tree.tables-per-namespace=20 \
		-Ddataset.tree.views-per-namespace=10 \
		-Dworkload.create-commits.table-commits-throughput=50 \
		-Dworkload.create-commits.view-commits-throughput=25 \
		-Dworkload.create-commits.duration-in-minutes=5 $(GRADLE_OPTS)

.PHONY: minio-commits-stress
minio-commits-stress:
	./gradlew gatlingRun --simulation org.apache.polaris.benchmarks.simulations.CreateCommits \
		-Dconfig.file=$(MINIO_CONFIG) \
		-Ddataset.tree.namespace-width=5 \
		-Ddataset.tree.namespace-depth=5 \
		-Ddataset.tree.tables-per-namespace=50 \
		-Ddataset.tree.views-per-namespace=25 \
		-Dworkload.create-commits.table-commits-throughput=100 \
		-Dworkload.create-commits.view-commits-throughput=50 \
		-Dworkload.create-commits.duration-in-minutes=10 $(GRADLE_OPTS)

# Weighted workload profiles
.PHONY: minio-weighted-light
minio-weighted-light:
	./gradlew gatlingRun --simulation org.apache.polaris.benchmarks.simulations.WeightedWorkloadOnTreeDataset \
		-Dconfig.file=$(MINIO_CONFIG) \
		-Dworkload.weighted-workload-on-tree-dataset.duration-in-minutes=2 $(GRADLE_OPTS)

.PHONY: minio-weighted-medium
minio-weighted-medium:
	./gradlew gatlingRun --simulation org.apache.polaris.benchmarks.simulations.WeightedWorkloadOnTreeDataset \
		-Dconfig.file=$(MINIO_CONFIG) \
		-Ddataset.tree.namespace-width=3 \
		-Ddataset.tree.namespace-depth=3 \
		-Ddataset.tree.tables-per-namespace=10 \
		-Ddataset.tree.views-per-namespace=5 \
		-Dworkload.weighted-workload-on-tree-dataset.duration-in-minutes=5 $(GRADLE_OPTS)

.PHONY: minio-weighted-high
minio-weighted-high:
	./gradlew gatlingRun --simulation org.apache.polaris.benchmarks.simulations.WeightedWorkloadOnTreeDataset \
		-Dconfig.file=$(MINIO_CONFIG) \
		-Ddataset.tree.namespace-width=4 \
		-Ddataset.tree.namespace-depth=4 \
		-Ddataset.tree.tables-per-namespace=20 \
		-Ddataset.tree.views-per-namespace=10 \
		-Dworkload.weighted-workload-on-tree-dataset.duration-in-minutes=10 $(GRADLE_OPTS)

.PHONY: minio-weighted-stress
minio-weighted-stress:
	./gradlew gatlingRun --simulation org.apache.polaris.benchmarks.simulations.WeightedWorkloadOnTreeDataset \
		-Dconfig.file=$(MINIO_CONFIG) \
		-Ddataset.tree.namespace-width=5 \
		-Ddataset.tree.namespace-depth=5 \
		-Ddataset.tree.tables-per-namespace=50 \
		-Ddataset.tree.views-per-namespace=25 \
		-Dworkload.weighted-workload-on-tree-dataset.duration-in-minutes=30 $(GRADLE_OPTS)

# Full benchmark suite
.PHONY: minio-full-benchmark
minio-full-benchmark: minio-create minio-read minio-read-update minio-commits minio-weighted
	@echo "Full MinIO benchmark suite completed. Run 'make reports-list' to view results."

.PHONY: minio-full-benchmark-light
minio-full-benchmark-light: minio-create-light minio-read-light minio-mixed-light minio-commits-light minio-weighted-light
	@echo "Light MinIO benchmark suite completed. Run 'make reports-list' to view results."

.PHONY: minio-full-benchmark-medium
minio-full-benchmark-medium: minio-create-medium minio-read-medium minio-mixed-medium minio-commits-medium minio-weighted-medium
	@echo "Medium MinIO benchmark suite completed. Run 'make reports-list' to view results."

.PHONY: minio-full-benchmark-high
minio-full-benchmark-high: minio-create-high minio-read-high minio-mixed-high minio-commits-high minio-weighted-high
	@echo "High MinIO benchmark suite completed. Run 'make reports-list' to view results."

.PHONY: minio-full-benchmark-stress
minio-full-benchmark-stress: minio-stress minio-read-stress minio-mixed-stress minio-commits-stress minio-weighted-stress
	@echo "Stress MinIO benchmark suite completed. Run 'make reports-list' to view results."

# Help target for MinIO commands
.PHONY: minio-help
minio-help:
	@echo "MinIO S3 Tables Benchmark Commands"
	@echo "==================================="
	@echo ""
	@echo "Prerequisites:"
	@echo "  export JAVA_HOME=/usr/lib/jvm/java-21-openjdk"
	@echo "  Create application.conf with SIGV4 credentials"
	@echo ""
	@echo "Base Commands (uses application.conf settings):"
	@echo "  make minio-create        - Create namespaces, tables, and views"
	@echo "  make minio-read          - Read existing dataset"
	@echo "  make minio-read-update   - Mixed read/write workload"
	@echo "  make minio-commits       - Create commits on tables/views"
	@echo "  make minio-weighted      - Weighted workload simulation"
	@echo ""
	@echo "Load Profiles (Create Dataset):"
	@echo "  make minio-create-light  - 1 NS, 2 tables, 1 view, 5 concurrency"
	@echo "  make minio-create-medium - 13 NS, 90 tables, 45 views, 20 concurrency"
	@echo "  make minio-create-high   - 85 NS, 1,280 tables, 640 views, 50 concurrency"
	@echo "  make minio-stress        - 781 NS, 31K tables, 15K views, 100 concurrency"
	@echo ""
	@echo "Load Profiles (Read Dataset):"
	@echo "  make minio-read-light    - 5 concurrent readers"
	@echo "  make minio-read-medium   - 20 concurrent readers"
	@echo "  make minio-read-high     - 50 concurrent readers"
	@echo "  make minio-read-stress   - 100 concurrent readers"
	@echo ""
	@echo "Load Profiles (Mixed Read/Write):"
	@echo "  make minio-mixed-light   - 20 ops/sec, 80% reads, 2 min"
	@echo "  make minio-mixed-medium  - 50 ops/sec, 70% reads, 5 min"
	@echo "  make minio-mixed-high    - 200 ops/sec, 60% reads, 10 min"
	@echo "  make minio-mixed-stress  - 500 ops/sec, 50% reads, 15 min"
	@echo ""
	@echo "Load Profiles (Commits):"
	@echo "  make minio-commits-light  - 7 commits/sec (5 table, 2 view), 1 min"
	@echo "  make minio-commits-medium - 30 commits/sec (20 table, 10 view), 3 min"
	@echo "  make minio-commits-high   - 75 commits/sec (50 table, 25 view), 5 min"
	@echo "  make minio-commits-stress - 150 commits/sec (100 table, 50 view), 10 min"
	@echo ""
	@echo "Load Profiles (Weighted Workload):"
	@echo "  make minio-weighted-light  - 2 min duration"
	@echo "  make minio-weighted-medium - 5 min duration"
	@echo "  make minio-weighted-high   - 10 min duration"
	@echo "  make minio-weighted-stress - 30 min duration"
	@echo ""
	@echo "Custom Parameters (pass your own values):"
	@echo "  make minio-create-custom NS_WIDTH=4 NS_DEPTH=4 TABLES=20 VIEWS=10 CREATE_CONCURRENCY=50"
	@echo "  make minio-read-custom READ_CONCURRENCY=100"
	@echo "  make minio-mixed-custom THROUGHPUT=1000 READ_RATIO=0.8 DURATION=10"
	@echo "  make minio-commits-custom TABLE_COMMITS=500 VIEW_COMMITS=200 DURATION=5"
	@echo "  make minio-weighted-custom DURATION=30"
	@echo ""
	@echo "Enterprise Scale (production-level):"
	@echo "  make minio-read-enterprise    - 500 concurrent readers"
	@echo "  make minio-mixed-enterprise   - 10K ops/sec, 70% reads, 15 min"
	@echo "  make minio-commits-enterprise - 3K commits/sec (2K table, 1K view), 10 min"
	@echo "  make minio-mixed-extreme      - 50K ops/sec, 60% reads, 20 min"
	@echo "  make minio-commits-extreme    - 15K commits/sec (10K table, 5K view), 15 min"
	@echo ""
	@echo "Full Benchmark Suites (runs create, read, mixed, commits, weighted):"
	@echo "  make minio-full-benchmark        - Run all base simulations"
	@echo "  make minio-full-benchmark-light  - Run all light load simulations"
	@echo "  make minio-full-benchmark-medium - Run all medium load simulations"
	@echo "  make minio-full-benchmark-high   - Run all high load simulations"
	@echo "  make minio-full-benchmark-stress - Run all stress test simulations"
	@echo ""
	@echo "Reports:"
	@echo "  make reports-list        - List all generated reports"
	@echo "  make reports-clean       - Delete all reports"
	@echo ""
	@echo "Custom Config:"
	@echo "  make minio-create MINIO_CONFIG=/path/to/custom.conf"
