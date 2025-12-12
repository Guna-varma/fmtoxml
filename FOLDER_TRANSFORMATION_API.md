# Folder Transformation API Documentation

## Overview

The Folder Transformation API allows you to upload a ZIP file containing multiple FrameMaker files (.fm/.mif) and process them in batch, maintaining the original folder structure and preserving additional folders like Images and logo.

## Endpoint

**POST** `/api/transform/folder-to-dita`

## Request

- **Content-Type:** `multipart/form-data`
- **Parameter:** `zipFile` (required)
- **File Format:** ZIP archive
- **Maximum Size:** 500MB

### Request Example (cURL)

```bash
curl -X POST "http://localhost:8080/api/transform/folder-to-dita" \
  -H "Content-Type: multipart/form-data" \
  -F "zipFile=@your_folder.zip"
```

### Request Example (Postman)

1. Select **POST** method
2. URL: `http://localhost:8080/api/transform/folder-to-dita`
3. Go to **Body** tab
4. Select **form-data**
5. Add key: `zipFile`, type: **File**
6. Select your ZIP file
7. Click **Send**

## ZIP File Structure

Your ZIP file should contain:

```
your_folder.zip
├── file1.fm
├── file2.mif
├── subfolder/
│   └── file3.fm
├── Images/
│   ├── image1.eps
│   └── image2.png
└── logo/
    └── company_logo.png
```

**Requirements:**
- At least one `.fm` or `.mif` file must be present
- Additional folders (Images, logo, etc.) are optional and will be preserved in output
- Folder names are case-insensitive for: Images, images, logo, Logo, logos, Logos

## Response

### Success Response (202 Accepted)

```json
{
    "jobId": "550e8400-e29b-41d4-a716-446655440000",
    "status": "PENDING",
    "message": "Folder transformation job submitted successfully",
    "createdAt": "2024-12-12T10:00:00"
}
```

### Error Responses

#### 400 Bad Request - Empty File
```json
{
    "timestamp": "2024-12-12T10:00:00",
    "status": 400,
    "error": "Bad Request",
    "message": "ZIP file cannot be empty"
}
```

#### 400 Bad Request - Invalid Format
```json
{
    "timestamp": "2024-12-12T10:00:00",
    "status": 400,
    "error": "Bad Request",
    "message": "Invalid file format. Only .zip files are supported"
}
```

#### 413 Payload Too Large
```json
{
    "timestamp": "2024-12-12T10:00:00",
    "status": 413,
    "error": "Payload Too Large",
    "message": "ZIP file size exceeds maximum allowed limit of 500MB"
}
```

#### 500 Internal Server Error
```json
{
    "timestamp": "2024-12-12T10:00:00",
    "status": 500,
    "error": "Internal Server Error",
    "message": "Error processing ZIP file: [error details]"
}
```

## Processing Flow

1. **Upload:** ZIP file is uploaded and stored
2. **Extraction:** ZIP is extracted to a temporary directory
3. **Discovery:** All `.fm` and `.mif` files are found recursively
4. **Validation:** Ensures at least one FrameMaker file exists
5. **Processing:** Each file is transformed asynchronously
6. **Merging:** Results are merged into a unified output structure
7. **Preservation:** Additional folders (Images, logo) are copied to output
8. **Cleanup:** Temporary files are removed
9. **Completion:** Job status is updated to COMPLETED

## Output Structure

The transformed output maintains the same folder structure:

```
output/
├── main.ditamap
├── table-of-contents/
│   └── toc.xml (and any additional TOC files)
├── xml/
│   ├── topic_001.xml
│   ├── topic_002.xml
│   └── ... (all topics from all files)
├── images/
│   ├── *.eps (extracted from FrameMaker files)
│   └── *.png / *.jpg
├── chapters/
│   ├── chapter_01.ditamap
│   └── ... (all chapter maps)
├── Images/ (preserved from input ZIP)
│   └── [original images]
└── logo/ (preserved from input ZIP)
    └── [original logos]
```

## Job Status Tracking

Use the same status endpoints as single file transformation:

1. **GET** `/api/transform/{jobId}/status` - Get job status
2. **GET** `/api/transform/{jobId}/details` - Get job details
3. **GET** `/api/transform/{jobId}/result` - Download result ZIP

## Validation Features

### Production-Ready Validation

- ✅ **File Format Validation:** Only `.zip` files accepted
- ✅ **File Size Validation:** Maximum 500MB
- ✅ **Content Validation:** At least one `.fm` or `.mif` file required
- ✅ **ZIP Slip Prevention:** Security check prevents directory traversal attacks
- ✅ **Entity Validation:** Jakarta Bean Validation on all entities
- ✅ **Input Validation:** `@NotNull`, `@NotBlank` annotations on parameters
- ✅ **Error Handling:** Global exception handler for consistent error responses

## Error Handling

The API includes comprehensive error handling:

- **Validation Errors:** Return 400 with detailed error messages
- **Processing Errors:** Individual file failures don't stop the entire job
- **Failed Files:** Tracked separately in job details
- **Cleanup:** Automatic cleanup of temporary files even on failure

## Best Practices

1. **ZIP Structure:** Organize files logically in subdirectories
2. **File Naming:** Use descriptive names to avoid conflicts
3. **Size Management:** Keep ZIP files under 500MB for optimal performance
4. **Monitoring:** Check job status regularly for large batches
5. **Error Recovery:** Review failed files list if job status is COMPLETED with failures

## Example Workflow

```bash
# 1. Upload ZIP file
POST /api/transform/folder-to-dita
Response: { "jobId": "abc-123", "status": "PENDING" }

# 2. Check status
GET /api/transform/abc-123/status
Response: "PROCESSING"

# 3. Wait and check again
GET /api/transform/abc-123/status
Response: "COMPLETED"

# 4. Download result
GET /api/transform/abc-123/result
Response: [ZIP file download]
```

## Security Features

- **ZIP Slip Prevention:** Validates all extracted paths
- **File Size Limits:** Prevents resource exhaustion
- **Input Sanitization:** Validates file names and paths
- **Temporary File Cleanup:** Automatic cleanup after processing

## Performance Considerations

- **Asynchronous Processing:** Jobs are processed in background
- **Batch Processing:** Multiple files processed efficiently
- **Resource Management:** Temporary files are cleaned up automatically
- **Scalability:** Uses thread pool for concurrent processing

