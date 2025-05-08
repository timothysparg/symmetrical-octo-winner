# Weather Application

This is a Spring Boot application that processes weather data, specifically rainfall data.

## Detailed Explanation

For a detailed explanation of how the `rainfallSyncJob` works, including Spring Batch concepts and data flow, please refer to the [explanation.md](explanation.md) file.

## Running the Rainfall Sync Job from the CLI

To run the `rainfallSyncJob` from the command line, use the following command:

```bash
# Using Maven
./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.batch.job.names=rainfallSyncJob"

# Or if you have the JAR file
java -jar weather-0.0.1-SNAPSHOT.jar --spring.batch.job.names=rainfallSyncJob
```
> [!IMPORTANT]  
> Despite having `spring.batch.job.enabled=false` in `application.properties`, the application includes a
> CommandLineRunner that automatically runs the `rainfallSyncJob` on startup. This means the job will run every time
> the application starts. It's recommended to use the commands above to run the job explicitly.

### Explanation

- `spring.batch.job.names=rainfallSyncJob`: This parameter specifies which job to run. In this case, it's the `rainfallSyncJob` defined in `BatchConfig.java`.

### Additional Parameters

You can also pass job parameters if needed:

```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.batch.job.names=rainfallSyncJob --date=2023-06-01"
```

This would pass a parameter named "date" with the value "2023-06-01" to the job.

## Configuration

The application has the following property in `application.properties`:

```properties
spring.batch.job.enabled=false
```

This property is intended to prevent Spring Batch jobs from running automatically at startup, but see the important note above about the CommandLineRunner.
