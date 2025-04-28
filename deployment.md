# Forum Application Deployment Guide

This document provides comprehensive instructions for deploying the Forum application using Docker and Kubernetes, and includes information on database configuration and migration.

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Local Development Setup](#local-development-setup)
3. [Docker Deployment](#docker-deployment)
4. [Kubernetes Deployment](#kubernetes-deployment)
5. [Database Setup and Migration](#database-setup-and-migration)
6. [Configuration Reference](#configuration-reference)
7. [Security Considerations](#security-considerations)
8. [Troubleshooting](#troubleshooting)

## Prerequisites

Before you begin, make sure you have the following installed:

- Java Development Kit (JDK) 21 or newer
- Maven 3.6+
- Docker and Docker Compose
- Kubernetes cluster (for Kubernetes deployment)
- kubectl command-line tool
- MySQL client (for database operations)

## Local Development Setup

### Running with H2 Database (Default)

The application uses H2 database by default for development:

```bash
# Build the application
mvn clean package

# Run the application
java -jar target/*.jar
```

The application will be available at http://localhost:8080.

## Docker Deployment

### Building the Docker Image

```bash
# Build the image
docker build -t forum-app:latest .
```

### Running with Docker Compose

The docker-compose.yml file is configured to start both the application and a MySQL database:

```bash
# Start the application and database
docker-compose up -d

# Check the logs
docker-compose logs -f
```

The application will be available at http://localhost:8080.

### Stopping the Application

```bash
docker-compose down
```

To also remove the volumes (which will delete all data):

```bash
docker-compose down -v
```

## Kubernetes Deployment

### Prerequisites

- A running Kubernetes cluster
- kubectl configured to communicate with your cluster
- Container registry to store your Docker image

### Deploy to Kubernetes

1. **Build and push the Docker image:**

   ```bash
   # Set your registry
   export DOCKER_REGISTRY=your-registry.com

   # Build and tag the image
   docker build -t ${DOCKER_REGISTRY}/forum-app:latest .

   # Push the image to the registry
   docker push ${DOCKER_REGISTRY}/forum-app:latest
   ```

2. **Configure your Kubernetes manifests:**

   Update the `k8s/deployment.yml` file to use your image registry:

   ```bash
   # Replace placeholder with your actual registry
   sed -i 's|${DOCKER_REGISTRY}|your-registry.com|g' k8s/deployment.yml
   ```

3. **Create MySQL database in Kubernetes (if not using an external database):**

   ```bash
   # Create a persistent volume claim for MySQL
   kubectl apply -f k8s/mysql-pvc.yml

   # Deploy MySQL
   kubectl apply -f k8s/mysql-deployment.yml
   kubectl apply -f k8s/mysql-service.yml
   ```

4. **Apply the ConfigMap and Secret:**

   ```bash
   # Apply ConfigMap
   kubectl apply -f k8s/configmap.yml

   # Create secrets
   # For production, replace the placeholder values in secret.yml first
   kubectl apply -f k8s/secret.yml
   ```

5. **Deploy the application:**

   ```bash
   kubectl apply -f k8s/deployment.yml
   kubectl apply -f k8s/service.yml
   ```

6. **Verify the deployment:**

   ```bash
   kubectl get pods
   kubectl get services
   ```

7. **Access the application:**

   If using ClusterIP service:
   ```bash
   # Port-forward to local machine
   kubectl port-forward svc/forum-app 8080:8080
   ```

   If using NodePort or LoadBalancer, check the service details:
   ```bash
   kubectl get svc forum-app -o wide
   ```

## Database Setup and Migration

### MySQL Setup

#### Setting Up MySQL User and Database

For local or custom MySQL installation:

```bash
# Connect to MySQL as root
mysql -u root -p

# Create the database and user
CREATE DATABASE forumdb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'forumuser'@'%' IDENTIFIED BY 'forumpass';
GRANT ALL PRIVILEGES ON forumdb.* TO 'forumuser'@'%';
FLUSH PRIVILEGES;
```

### Migrating from H2 to MySQL

To migrate data from H2 to MySQL:

1. **Export data from H2:**

   Create a data export SQL script in your application by implementing a service that uses JPA repository methods to fetch all data and generate SQL insert statements for MySQL.

2. **Configure application to use MySQL:**

   Update `application.properties` or environment variables:

   ```properties
   # H2 configuration (original)
   # spring.datasource.url=jdbc:h2:file:./test2
   # spring.datasource.driverClassName=org.h2.Driver
   # spring.datasource.username=sa
   # spring.datasource.password=
   # spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
   
   # MySQL configuration
   spring.datasource.url=jdbc:mysql://localhost:3306/forumdb?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
   spring.datasource.driverClassName=com.mysql.cj.jdbc.Driver
   spring.datasource.username=forumuser
   spring.datasource.password=forumpass
   spring.jpa.database-platform=org.hibernate.dialect.MySQL8Dialect
   spring.jpa.hibernate.ddl-auto=update
   ```

3. **Import data to MySQL:**

   ```bash
   # Run the SQL script on MySQL
   mysql -u forumuser -p forumdb < data_export.sql
   ```

### Database Backup and Restore

#### Backing up MySQL Database

```bash
# In Docker Compose environment
docker exec forum-mysql mysqldump -u forumuser -pforumpass forumdb > backup.sql

# In Kubernetes
kubectl exec [mysql-pod-name] -- mysqldump -u forumuser -pforumpass forumdb > backup.sql
```

#### Restoring MySQL Database

```bash
# In Docker Compose environment
cat backup.sql | docker exec -i forum-mysql mysql -u forumuser -pforumpass forumdb

# In Kubernetes
cat backup.sql | kubectl exec -i [mysql-pod-name] -- mysql -u forumuser -pforumpass forumdb
```

## Configuration Reference

### Spring Boot Properties

The following properties can be configured:

| Property | Description | Default |
|----------|-------------|---------|
| `spring.datasource.url` | Database connection URL | `jdbc:h2:file:./test2` |
| `spring.datasource.username` | Database username | `sa` |
| `spring.datasource.password` | Database password | ` ` |
| `spring.jpa.hibernate.ddl-auto` | Database schema strategy | `update` |
| `spring.jpa.show-sql` | Show SQL queries | `false` |
| `server.port` | Application port | `8080` |

### Environment Variables

All Spring Boot properties can be overridden with environment variables by converting:
- Dots (.) to underscores
- Making all letters uppercase

Example: `SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/forumdb`

## Security Considerations

### Production Readiness Checklist

1. **Secure Passwords:**
   - Use strong, unique passwords for database
   - Store sensitive data in Kubernetes Secrets
   - Use a secure password manager for team access

2. **SSL/TLS:**
   - Configure HTTPS in production
   - Use TLS for database connections

3. **JWT Security:**
   - Use a strong, unique secret for JWT tokens
   - Configure appropriate token expiration

4. **Database Security:**
   - Use principle of least privilege for database users
   - Restrict network access to the database
   - Regularly backup the database

5. **Container Security:**
   - Keep base images updated
   - Use non-root users in containers (already configured)
   - Scan images for vulnerabilities

## Troubleshooting

### Common Issues

#### Application Won't Start

**Symptoms:** Application fails to start, logs show database connection errors.

**Solution:** 
- Verify database is running and accessible
- Check connection credentials
- Ensure database exists and user has proper permissions

#### Database Migration Issues

**Symptoms:** Application starts but fails with SQL errors or missing tables.

**Solution:**
- Set `spring.jpa.hibernate.ddl-auto=create` temporarily to recreate schema
- Check for database compatibility issues
- Review entity changes against existing schema

#### Kubernetes Deployment Issues

**Symptoms:** Pods fail to start or are stuck in pending/crashloopbackoff state.

**Solution:**
- Check pod logs: `kubectl logs [pod-name]`
- Verify ConfigMap and Secret are properly mounted
- Check for resource constraints (CPU/memory)
- Verify image pull policy and registry access

#### Performance Issues

**Symptoms:** Application is slow or unresponsive under load.

**Solution:**
- Increase container resources in deployment.yml
- Optimize database queries
- Configure connection pooling
- Add caching where appropriate

