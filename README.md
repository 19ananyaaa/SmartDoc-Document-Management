# SmartDoc — Smart Document Management and Capture Platform

A web-based document management system developed using Advanced Java technologies to simplify document storage, retrieval, and text extraction from scanned documents.

## About the Project

SmartDoc is designed to provide a centralized platform for managing digital documents. It combines Java-based web technologies with MySQL database connectivity and Optical Character Recognition (OCR) to make document handling more efficient.

## Features

* User authentication and role-based access
* Document upload and management
* Document metadata storage
* OCR-based text extraction using Tess4J
* Document search and retrieval
* MySQL database integration
* Admin dashboard
* Session management and access control

*Note: Features listed above should be retained in this README only if implemented and working in the current version.*

## Technology Stack

| Component            | Technology                 |
| -------------------- | -------------------------- |
| Programming Language | Java 17                    |
| Backend              | Jakarta Servlets, JDBC     |
| Frontend             | JSP, HTML, CSS, JavaScript |
| Database             | MySQL                      |
| Web Server           | Apache Tomcat 10           |
| OCR                  | Tess4J / Tesseract OCR     |
| Build Tool           | Maven                      |

## Project Structure

The exact directory structure may vary depending on the current implementation.

```text
SmartDoc/
├── src/
│   └── main/
│       ├── java/
│       └── webapp/
├── pom.xml
├── database/
└── README.md
```

## Prerequisites

Install the following before running the application:

* JDK 17
* Apache Maven
* MySQL Server
* Apache Tomcat 10.1
* Tesseract OCR, if OCR functionality is enabled

## Setup and Installation

### 1. Clone the Repository

```bash
git clone https://github.com/19ananyaaa/SmartDoc-Document-Management.git
```

### 2. Open the Project

Open the cloned project in your preferred Java IDE, such as VS Code or IntelliJ IDEA.

### 3. Configure the Database

Create the required MySQL database and tables using the project's SQL schema.

Configure the database connection using your local credentials. Do not commit passwords or other sensitive configuration values to GitHub.

### 4. Build the Project

```bash
mvn clean package
```

### 5. Deploy the Application

Deploy the generated WAR file to Apache Tomcat, or use the deployment method configured for the project.

Start Tomcat and open the application using its configured local URL.

## Security Considerations

* Never commit database passwords or administrator credentials.
* Validate uploaded files and enforce appropriate file-size limits.
* Use prepared statements for database operations.
* Protect authenticated pages and enforce authorization checks.
* Store passwords using a suitable password-hashing algorithm.

## Future Enhancements

* Advanced document classification
* Improved full-text search
* Document version management
* Enhanced audit logging
* Cloud storage integration
* Additional OCR language support

## Project Information

**Project Name:** SmartDoc — Smart Document Management and Capture Platform

**Domain:** Advanced Java / Web Application Development
