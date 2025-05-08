# RainfallSyncJob Explanation

## Overview
The `rainfallSyncJob` is a Spring Batch job designed to synchronize rainfall data from the Hong Kong Weather API to a local file system. The job fetches current rainfall data from various weather stations, detects changes compared to previously stored data, and persists new or updated information.

## System Architecture Diagram

![RainfallSyncJob Architecture](diagram.svg)

The diagram shows:
- The Spring Batch Job (`rainfallSyncJob`) with its chunk-based step (`importRainfallStep`)
- The three main components of the step: `RainfallReader` (ItemReader), `ChangeDetectionProcessor` (ItemProcessor), and `FileWriter` (ItemWriter)
- External components: Hong Kong Weather API and FileSystemRepository
- File System Storage with two directories: downstream (all data) and upstream (only new/updated data)
- The data flow between components, numbered to show the sequence of operations

## Spring Batch Concepts

### Job
A Job in Spring Batch represents a complete batch process. In this application, `rainfallSyncJob` is the main job that orchestrates the entire process of fetching, processing, and storing rainfall data. The job is configured in `BatchConfig.java` and is executed by the `JobLauncher` when the application starts.

Key components:
- **JobBuilder**: Used to construct the job with a name and repository
- **RunIdIncrementer**: Ensures each job execution has a unique identifier
- **JobRepository**: Stores metadata about job executions, including status, start/end times, and execution parameters

### Step
A Step is a domain object that encapsulates an independent, sequential phase of a batch job. The `rainfallSyncJob` contains a single step called `importRainfallStep`, which is responsible for reading, processing, and writing rainfall data.

Key components:
- **StepBuilder**: Used to construct the step with a name and repository
- **Chunk Processing**: The step processes data in chunks (10 items at a time), which improves performance and resource utilization

### ItemReader
An ItemReader is responsible for providing data one item at a time from a source. In this application, `RainfallReader` implements the `ItemReader<RainfallData>` interface to fetch rainfall data from the Hong Kong Weather API.

Key features:
- Connects to the API endpoint using RestTemplate
- Parses JSON response into RainfallData objects
- Maintains state between read operations
- Returns null when all items have been read, signaling the end of available data

### ItemProcessor
An ItemProcessor is responsible for transforming or filtering items during processing. In this application, `ChangeDetectionProcessor` implements the `ItemProcessor<RainfallData, RainfallData>` interface to detect new or changed rainfall data.

Key features:
- Compares incoming data with existing data from the repository
- Marks items as new or updated based on comparison
- Returns null for items that haven't changed significantly, which filters them out of the processing pipeline

### ItemWriter
An ItemWriter is responsible for outputting data in batches. In this application, `FileWriter` implements the `ItemWriter<RainfallData>` interface to write processed rainfall data to the file system.

Key features:
- Receives items in chunks (as configured in the step)
- Updates timestamps before writing
- Uses the repository to save all items in the chunk

### Transaction Management
Spring Batch manages transactions to ensure data integrity during batch processing. The `PlatformTransactionManager` is used to manage transactions for the chunk-oriented processing in the step.

## Data Flow

As illustrated in the diagram, the data flows through the system in the following sequence:

1. **Fetch JSON Data**: The `RainfallReader` fetches rainfall data from the Hong Kong Weather API, converting the JSON response into `RainfallData` objects.

2. **Check if Data Exists/Changed**: The `ChangeDetectionProcessor` interacts with the `FileSystemRepository` to compare each incoming item with existing data:
   - If the item is new (not found in the repository), it's marked as new
   - If the item exists but has changed significantly, it's marked as updated
   - If the item hasn't changed significantly, it's filtered out (by returning null)

3. **Read Existing Data**: The `FileSystemRepository` reads existing data from the downstream directory to provide it to the `ChangeDetectionProcessor` for comparison.

4. **Write Processed Data**: The `FileWriter` sends the processed items to the `FileSystemRepository` for storage.

5. **Storage Operations**:
   - **5a. Write ALL Data**: All processed items are written to the downstream directory, serving as a complete repository.
   - **5b. Write NEW/UPDATED Data Only**: Only items marked as new or updated are written to the upstream directory, which could be used for further processing or integration with other systems.

The job is executed by the `JobLauncher` when the application starts, with unique parameters to identify each execution. When the job completes, its status is stored in the job repository.
