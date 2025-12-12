# FrameMaker to DITA Transformation REST API Documentation

## Overview

This is a **REST API web application** built with Spring Boot. All endpoints are accessible via HTTP requests.

## Base URL
```
http://localhost:8080/api/transform
```

## API Endpoints

### 1. Upload and Transform File

**POST** `/api/transform/framemaker-to-dita`

Upload a FrameMaker file (.fm or .mif) for transformation.

**Request:**
- Method: `POST`
- Content-Type: `multipart/form-data`
- Body: Form data with `file` parameter containing the FrameMaker file

**Response:**
```json
{
    "jobId": "550e8400-e29b-41d4-a716-446655440000",
    "status": "PENDING",
    "message": "Transformation job submitted successfully",
    "createdAt": "2024-01-01T10:00:00"
}
```

**Status Codes:**
- `200 OK` - File uploaded successfully, transformation started
- `400 Bad Request` - Invalid file or missing file parameter
- `500 Internal Server Error` - Server error

**Example using cURL:**
```bash
curl -X POST http://localhost:8080/api/transform/framemaker-to-dita \
  -F "file=@samples/sample.mif"
```

---

### 2. Get Job Status

**GET** `/api/transform/{jobId}/status`

Get the current status of a transformation job.

**Path Parameters:**
- `jobId` (string) - The job ID returned from the upload endpoint

**Response:**
Returns one of the following status strings:
- `PENDING` - Job is queued
- `PROCESSING` - Transformation is in progress
- `COMPLETED` - Transformation completed successfully
- `FAILED` - Transformation failed

**Status Codes:**
- `200 OK` - Status returned
- `404 Not Found` - Job ID not found

**Example using cURL:**
```bash
curl http://localhost:8080/api/transform/550e8400-e29b-41d4-a716-446655440000/status
```

---

### 3. Get Job Details

**GET** `/api/transform/{jobId}/details`

Get detailed information about a transformation job.

**Path Parameters:**
- `jobId` (string) - The job ID returned from the upload endpoint

**Response:**
```json
{
    "jobId": "550e8400-e29b-41d4-a716-446655440000",
    "status": "COMPLETED",
    "createdAt": "2024-01-01T10:00:00",
    "outputPath": "C:\\Users\\...\\framemaker\\output\\550e8400-e29b-41d4-a716-446655440000",
    "message": "Transformation completed successfully"
}
```

**Status Codes:**
- `200 OK` - Job details returned
- `404 Not Found` - Job ID not found

**Example using cURL:**
```bash
curl http://localhost:8080/api/transform/550e8400-e29b-41d4-a716-446655440000/details
```

---

### 4. Get Transformation Result

**GET** `/api/transform/{jobId}/result`

Download the transformed DITA files as a ZIP archive.

**Path Parameters:**
- `jobId` (string) - The job ID returned from the upload endpoint

**Response Headers:**
- Content-Type: `application/octet-stream`
- Content-Disposition: `attachment; filename="transformation_{jobId}.zip"`
- Content-Length: Size of ZIP file in bytes

**Response Body:**
- Binary ZIP file containing the transformed DITA structure

**Status Codes:**
- `200 OK` - ZIP file returned
- `404 Not Found` - Job ID not found or transformation not completed

**Important Notes:**
- This endpoint returns a **binary file**, not JSON
- In Postman: Use **"Send and Download"** button (dropdown next to Send) to download the file
- The response will appear as binary/text in Postman's body viewer - this is normal!

**Example using cURL:**
```bash
curl -O -J http://localhost:8080/api/transform/550e8400-e29b-41d4-a716-446655440000/result
```

The `-O` flag saves the file, and `-J` uses the filename from the Content-Disposition header.

---

## Workflow Example

1. **Upload a file:**
   ```bash
   curl -X POST http://localhost:8080/api/transform/framemaker-to-dita \
     -F "file=@samples/sample.mif"
   ```
   Response: `{"jobId": "abc123...", "status": "PENDING", ...}`

2. **Check status (poll until COMPLETED):**
   ```bash
   curl http://localhost:8080/api/transform/abc123.../status
   ```
   Response: `PROCESSING` (then later `COMPLETED`)

3. **Download result:**
   ```bash
   curl -O http://localhost:8080/api/transform/abc123.../result
   ```

---

## Sample Files

Sample MIF files are available in the `samples/` directory:
- `sample.mif` - Basic sample with chapters and sections
- `sample_complex.mif` - More complex sample with images and tables

---

## Output Structure

The ZIP file contains:
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
│   └── *.eps (if any)
└── chapters/
    ├── chapter_01.ditamap
    └── ...
```

