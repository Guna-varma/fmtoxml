# FrameMaker to DITA Transformation Web Application

A production-ready Spring Boot REST API web application that transforms FrameMaker files (.fm/.mif) into DITA (Darwin Information Typing Architecture) format.

## 🎯 Overview

This is a **web application** that provides REST API endpoints for:
- Uploading FrameMaker files
- Asynchronously transforming them to DITA format
- Tracking transformation job status
- Downloading transformed DITA files as ZIP archives

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Maven 3.6+
- PostgreSQL (running on localhost:5432)
- Database: `spear` (must exist)

### Start Application

```bash
mvn spring-boot:run
```

The application will start on **http://localhost:8080**

### Build and Run JAR

```bash
mvn clean package
java -jar target/framemaker-dita-transformation-1.0.0.jar
```

## 📡 REST API Endpoints

All endpoints are available at: `http://localhost:8080/api/transform`

### 1. Upload and Transform File

**POST** `/api/transform/framemaker-to-dita`

Upload a FrameMaker file (.fm or .mif) for transformation.

**Request:**
- Content-Type: `multipart/form-data`
- Parameter: `file` (the FrameMaker file)

**Response:**
```json
{
    "jobId": "550e8400-e29b-41d4-a716-446655440000",
    "status": "PENDING",
    "message": "Transformation job submitted successfully",
    "createdAt": "2024-12-12T10:00:00"
}
```

### 2. Upload and Transform Folder (ZIP)

**POST** `/api/transform/folder-to-dita`

Upload a ZIP file containing multiple FrameMaker files (.fm or .mif) and folder structure for batch transformation.

**Request:**
- Content-Type: `multipart/form-data`
- Parameter: `zipFile` (ZIP file containing .fm/.mif files and folders)
- Maximum file size: 500MB

**ZIP Structure:**
The ZIP file should contain:
- Multiple `.fm` or `.mif` files (can be in subdirectories)
- Optional folders: `Images`, `images`, `logo`, `Logo`, `logos`, `Logos` (will be preserved in output)

**Response:**
```json
{
    "jobId": "550e8400-e29b-41d4-a716-446655440000",
    "status": "PENDING",
    "message": "Folder transformation job submitted successfully",
    "createdAt": "2024-12-12T10:00:00"
}
```

**Output Structure:**
The transformed output maintains the same folder structure:
```
output/
├── main.ditamap
├── table-of-contents/
│   └── toc.xml
├── xml/
│   ├── topic_001.xml
│   ├── topic_002.xml
│   └── ...
├── images/
│   ├── *.eps
│   └── *.png / *.jpg
├── chapters/
│   ├── chapter_01.ditamap
│   └── ...
├── Images/ (preserved from input)
└── logo/ (preserved from input)
```

**Validation:**
- ZIP file must not be empty
- ZIP file must contain at least one `.fm` or `.mif` file
- File size must not exceed 500MB
- Invalid ZIP format will be rejected

### 3. Get Job Status

**GET** `/api/transform/{jobId}/status`

Get the current status of a transformation job.

**Response:** Plain text
- `PENDING`
- `PROCESSING`
- `COMPLETED`
- `FAILED`

### 4. Get Job Details

**GET** `/api/transform/{jobId}/details`

Get detailed information about a transformation job.

**Response:**
```json
{
    "jobId": "550e8400-e29b-41d4-a716-446655440000",
    "status": "COMPLETED",
    "createdAt": "2024-12-12T10:00:00",
    "outputPath": "/path/to/output"
}
```

### 5. Download Transformation Result

**GET** `/api/transform/{jobId}/result`

Download the transformed DITA files as a ZIP archive.

**Response:** Binary ZIP file download

**⚠️ IMPORTANT - In Postman:**
- This endpoint returns a **BINARY ZIP FILE**, not JSON
- Use **"Send and Download"** button (dropdown next to Send) to download
- If you see binary data (PK, NUL characters) in the response body, that's normal!
- See `HOW_TO_DOWNLOAD_ZIP_IN_POSTMAN.md` for detailed instructions

## 🗄️ Database Configuration

The application uses **PostgreSQL** database.

**Connection Details:**
- Host: `localhost`
- Port: `5432`
- Database: `spear`
- Username: `postgres`
- Password: `1234`

**Configuration:** `src/main/resources/application.yml`

All settings can be overridden via environment variables:
- `DB_HOST`
- `DB_PORT`
- `DB_USER`
- `DB_PASSWORD`
- `DB_NAME`
- `SERVER_PORT`

**Table:** `transformation_jobs` (auto-created by JPA/Hibernate)

## 📦 Output Structure

Transformed files are organized as:

```
output/
├── main.ditamap
├── table-of-contents/
│   └── toc.xml
├── xml/
│   ├── topic_001.xml
│   ├── topic_002.xml
│   └── ...
├── images/
│   ├── *.eps
│   └── *.png / *.jpg (if any)
└── chapters/
    ├── chapter_01.ditamap
    ├── chapter_02.ditamap
    └── ...
```

## 🔧 Technology Stack

- **Java 17**
- **Spring Boot 3.2.0**
- **PostgreSQL** (with JPA/Hibernate)
- **Maven**
- **dom4j** (XML processing)
- **JAXB**
- **Apache Commons IO/Compress**

## 📋 Features

- ✅ RESTful API for file upload and transformation
- ✅ **Batch folder transformation (ZIP upload)**
- ✅ Asynchronous job processing
- ✅ PostgreSQL database for job tracking
- ✅ Support for MIF format (.mif files)
- ✅ Image extraction (.eps files)
- ✅ Table of contents generation
- ✅ DITA topic and map generation
- ✅ ZIP download of transformed files
- ✅ **Production-ready validation and error handling**
- ✅ **Preserves folder structure (Images, logo folders)**

## 🧪 Testing

Run all tests:
```bash
mvn test
```

**Test Results:** ✅ All 11 tests passing

## 📝 API Documentation

See `API_Documentation.md` for detailed API documentation.

**Postman Collection:** Import `Postman_Collection.json` into Postman for easy testing.

## 🔒 Production Considerations

- Uses PostgreSQL for persistent storage
- Asynchronous processing for scalability
- **Comprehensive validation with Jakarta Bean Validation**
- **Global exception handler for consistent error responses**
- **Entity validation with constraints (NotBlank, Size, NotNull)**
- **File size validation and security checks (ZIP slip prevention)**
- RESTful API design
- Production-grade logging configuration
- Environment variable support for configuration
- **Input validation on all endpoints**

## ⚠️ Limitations

1. **Binary FrameMaker (.fm) files are NOT supported** - only MIF format
2. Complex table structures may require manual adjustment
3. Embedded graphics extraction depends on file references

## 📄 License

This project is provided as-is for transformation purposes.
