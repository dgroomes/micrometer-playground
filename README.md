# micrometer-playground

📚 Learning and exploring Micrometer.

> Vendor-neutral application observability facade
>
> -- <cite> https://micrometer.io </cite>


## Overview

**NOTE**: This project was developed on macOS. It is designed for my own personal use.

Micrometer is a mature and popular open source Java library to help you capture and publish metrics about your application.
It is maintained by VMWare, which is the same organization that maintains the Spring family of libraries. Micrometer is
the default metrics library in Spring Boot applications. This repository is me learning Micrometer by example. I will do
it by way of a simple example stack. I am not using Spring Boot, to keep things focused on Micrometer: 

* A program-under-observation
  * This is a fictional "data processing" program written in Java. This program is instrumented with Micrometer.
* A metrics database (InfluxDB)
  * InfluxDB is an open source time series database that's usually used for metrics.

This project is a corollary to my other project [dgroomes/open-telemetry-playground](https://github.com/dgroomes/open-telemetry-playground).
It is mostly a copy/paste of that project.


## Instructions

Follow these instructions to build and run the example system.

1. Pre-requisites: Java and Docker
    * I used Java 17.
2. Start the InfluxDB container
    * ```shell
      docker-compose up
      ```
3. Build the program distribution
    * ```shell
      ./gradlew installDist
      ```
4. Run the program
    * ```shell
      ./build/install/micrometer-playground/bin/micrometer-playground
      ```
    * The program will run indefinitely and continuously submit ILP-formatted (Influx Line Protocol) metrics into the
      InfluxDB database. You should see output like the following.
    * ```text
      $ ./build/install/micrometer-playground/bin/micrometer-playground
      20:39:05 [main] INFO dgroomes.Runner - Let's simulate some fictional data processing and instrument the program with Micrometer...
      20:39:05 [main] DEBUG io.micrometer.common.util.internal.logging.InternalLoggerFactory - Using SLF4J as the default logging framework
      20:39:05 [main] INFO io.micrometer.core.instrument.push.PushMeterRegistry - publishing metrics for InfluxMeterRegistry every 10s
      20:39:05 [main] INFO io.micrometer.influx.InfluxMeterRegistry - Using InfluxDB API version V1 to write metrics
      20:39:11 [influx-metrics-publisher] DEBUG io.micrometer.influx.InfluxMeterRegistry - influx database playground is ready to receive metrics
      20:39:11 [influx-metrics-publisher] DEBUG io.micrometer.influx.InfluxMeterRegistry - successfully sent 53 metrics to InfluxDB.
      20:39:21 [influx-metrics-publisher] DEBUG io.micrometer.influx.InfluxMeterRegistry - successfully sent 53 metrics to InfluxDB.
      20:39:31 [influx-metrics-publisher] DEBUG io.micrometer.influx.InfluxMeterRegistry - successfully sent 54 metrics to InfluxDB.
      ```
5. Inspect the metrics in InfluxDB
    * Start an `influx` session inside the InfluxDB container with the following command.
    * ```shell
      docker exec -it micrometer-playground-influxdb-1 influx -precision rfc3339
      ```
    * The `influx` session may remind you of a SQL sessions. In it, you can run commands like `SHOW DATABASES` and
      `SHOW MEASUREMENTS` to explore the data. We named our database `playground`. You should be able to connect to it
      by issuing a `use playground` command. Then, execute a `show measurements` command, and it will show the following
      metrics that have flowed from our program into the Influx database. It should look something like the following.
    * ```text
      $ docker exec -it micrometer-playground-influxdb-1 influx
      Connected to http://localhost:8086 version 1.8.10
      InfluxDB shell version: 1.8.10
      > use playground
      Using database playground
      > show measurements
      name: measurements
      name
      ----
      jvm_buffer_count
      jvm_buffer_memory_used
      jvm_buffer_total_capacity
      jvm_classes_loaded
      jvm_classes_unloaded
      jvm_gc_live_data_size
      jvm_gc_max_data_size
      jvm_gc_memory_allocated
      jvm_gc_memory_promoted
      jvm_gc_pause
      jvm_memory_committed
      jvm_memory_max
      jvm_memory_used
      jvm_threads_daemon
      jvm_threads_live
      jvm_threads_peak
      jvm_threads_started
      jvm_threads_states
      process_cpu_usage
      system_cpu_count
      system_cpu_usage
      system_load_average_1m
      ```
    * Let's inspect the memory usage over time for our "data processing" program. This is captured in the `jvm_memory_used`
      metric. Look at the below snippet for an example. The output shows the memory usage in MiB over time. The memory
      usage varies between 14Mib and 278MiB in the four-minute window shown below. 
    * ```text
      > SELECT SUM(value) / 1024 / 1024 AS "MiB" FROM "jvm_memory_used" WHERE area = 'heap' GROUP BY time(10s)
      name: jvm_memory_used
      time                 MiB
      ----                 ---
      2023-11-21T02:53:30Z 14.217994689941406
      2023-11-21T02:53:40Z 22.754737854003906
      2023-11-21T02:53:50Z 58.754737854003906
      2023-11-21T02:54:00Z 86.7547378540039
      2023-11-21T02:54:10Z 118.7547378540039
      2023-11-21T02:54:20Z 150.7547378540039
      2023-11-21T02:54:30Z 182.7547378540039
      2023-11-21T02:54:40Z 214.7547378540039
      2023-11-21T02:54:50Z 246.7547378540039
      2023-11-21T02:55:00Z 278.7547378540039
      2023-11-21T02:55:10Z 14.558074951171875
      2023-11-21T02:55:20Z 46.558074951171875
      2023-11-21T02:55:30Z 78.55807495117188
      2023-11-21T02:55:40Z 110.55807495117188
      2023-11-21T02:55:50Z 146.55807495117188
      2023-11-21T02:56:00Z 178.55807495117188
      2023-11-21T02:56:10Z 210.55807495117188
      2023-11-21T02:56:20Z 242.55807495117188
      2023-11-21T02:56:30Z 274.5580749511719
      2023-11-21T02:56:40Z 14.508644104003906
      2023-11-21T02:56:50Z 46.508644104003906
      2023-11-21T02:57:00Z 78.5086441040039
      2023-11-21T02:57:10Z 110.5086441040039
      2023-11-21T02:57:20Z 142.5086441040039
      2023-11-21T02:57:30Z 174.5086441040039
      ```
6. Stop the Java program
    * Press `Ctrl+C` to stop the program from the same terminal window where you ran the program.
7. Stop the Influx database
    * ```shell
        docker-compose down
      ```


## Wish List

General clean-ups, TODOs and things I wish to implement for this project:

* [x] DONE Scaffold
* [ ] Create a custom metric


## Reference

* [Micrometer docs: *Micrometer Influx*](https://micrometer.io/docs/registry/influx)
