⚡ Scalable Reactive Chat App
A highly scalable, reactive chat application built with Spring WebFlux, Kafka, and WebSocket, designed to handle real-time communication across multiple distributed instances — capable of effortlessly managing millions of concurrent WebSocket connections.

Using Docker, you can spin up multiple instances of the Netty-powered WebFlux server, each independently managing persistent WebSocket sessions. Apache Kafka acts as a central message broker — enabling each instance to consume and broadcast messages to connected clients with resilience and efficiency.

🏗️ Architecture Overview
Spring Boot WebFlux + Netty — Asynchronous, non-blocking foundation supporting high concurrency

Apache Kafka — Distributed message bus between all chat nodes for reliable, decoupled communication

WebSocket + Nginx — Persistent, low-latency, bi-directional messaging with Nginx for load balancing and long-lived connection support

Dockerized Deployment — Rapid scalability with containerized app and infrastructure

Kafka Consumers — Each instance operates in its own consumer group, broadcasting relevant messages to its connected clients

✨ Highlights
📡 Real-time messaging using backpressure-aware reactive streams

🕸️ Millions of concurrent WebSocket clients supported with minimal resource usage

🔄 Kafka as a fault-tolerant, decoupled messaging backbone

🔀 WebSocket load balancing via Nginx with support for long connection durations

♻️ Reactive & stateless architecture enables effortless horizontal scaling

🐳 One-command setup using Docker Compose for fast local development or deployment
