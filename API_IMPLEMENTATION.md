# API Implementation Documentation

Complete API documentation for FrameMaker to DITA Transformation API.

## Base URL

```
http://localhost:8080
```

---

## 🎯 Main API Endpoint

### POST `/api/transform/folder-to-dita`

**Upload folder → Get output folder**

This is the **primary endpoint** that combines validation and transformation in one API call.

#### Request

- **Method:** POST
- **Content-Type:** multipart/form-data
- **Parameters:**
  - `zipFile` (form-data, file, required): ZIP file containing FrameMaker files

#### Validation Rules (Applied Automatically)

- ✅ Folder must contain only `.fm` files OR only `.mif` files
- ❌ Mixed `.fm` + `.mif` is invalid
- ✅ Maximum file size: 500MB
- ✅ Image files accepted: `.eps`, `.png`, `.jpg`, `.jpeg`, `.gif`, `.svg`, `.bmp`, `.tiff`

#### Response (200 OK - Validation Passed)

```json
{
    "jobId": "550e8400-e29b-41d4-a716-446655440000",
    "status": "PENDING",
    "message": "Folder transformation job submitted successfully",
    "createdAt": "2024-12-12T10:00:00",
    "validationPassed": true,
    "validationMessage": "Validation successful: Folder contains 5 .fm file(s)",
    "fileType": "FM",
    "validatedFileCount": 5,
    "validatedFileNames": ["file1.fm", "file2.fm", "file3.fm", "file4.fm", "file5.fm"]
}
```

#### Response (400 Bad Request - Validation Failed)

```json
{
    "timestamp": "2024-12-12T10:00:00",
    "status": 400,
    "error": "Bad Request",
    "message": "Invalid folder structure: Mixed file types detected. Found 3 .fm files and 2 .mif files. Folder must contain only .fm files OR only .mif files, not both."
}
```

#### Error Responses

- **400 Bad Request:** Invalid ZIP format, empty file, or validation failed (mixed file types)
- **413 Payload Too Large:** File size exceeds 500MB
- **500 Internal Server Error:** Server error

#### Example Request (cURL)

```bash
curl -X POST "http://localhost:8080/api/transform/folder-to-dita" \
  -H "Content-Type: multipart/form-data" \
  -F "zipFile=@your_folder.zip"
```

#### Example Request (Postman)

1. Select **POST** method
2. URL: `http://localhost:8080/api/transform/folder-to-dita`
3. Go to **Body** tab
4. Select **form-data**
5. Add key: `zipFile`, type: **File**
6. Browse and select your ZIP file
7. Click **Send**

#### ⚙️ Why Status is "PENDING" Initially

**Important:** The API uses **asynchronous job processing**. When you submit a folder transformation request, the API immediately returns a response with `status: "PENDING"` - this is **expected behavior**, not an error.

##### How Asynchronous Processing Works

1. **Job Submission (Immediate Response)**
   - The API validates your ZIP file and folder structure
   - If validation passes, it creates a job with status `PENDING`
   - The job is saved to the database
   - The API **immediately returns** with `status: "PENDING"` and a `jobId`
   - The actual transformation starts **asynchronously in the background**

2. **Background Processing**
   - The transformation runs in a separate thread (using Spring's `@Async`)
   - Status automatically changes: `PENDING` → `PROCESSING` → `COMPLETED` (or `FAILED`)
   - You can check the status anytime using the status endpoint

##### Status Flow

```
PENDING → PROCESSING → COMPLETED
                          ↓
                       FAILED (if error occurs)
```

##### Why This Design?

✅ **Prevents Timeouts:** Large folders can take minutes to process - async prevents HTTP timeouts  
✅ **Better Performance:** Multiple jobs can run concurrently  
✅ **Non-Blocking:** API responds immediately, doesn't block the client  
✅ **Scalable:** Can handle many concurrent transformation requests  

##### How to Track Progress

After receiving the initial `PENDING` response:

1. **Check Status:**
   ```bash
   GET /api/transform/{jobId}/status
   ```
   Returns: `PENDING`, `PROCESSING`, `COMPLETED`, or `FAILED`

2. **Get Full Details:**
   ```bash
   GET /api/transform/{jobId}/details
   ```
   Returns complete job information including current status

3. **Download Result (when COMPLETED):**
   ```bash
   GET /api/transform/{jobId}/result
   ```
   Downloads the transformed folder as a ZIP file

##### Example: Complete Flow

**Step 1: Submit Job**
```bash
POST /api/transform/folder-to-dita
```

**Response (Immediate):**
```json
{
    "jobId": "b73949de-2719-4d1d-ab4d-4f630dd3230b",
    "status": "PENDING",  ← Normal! Job is queued
    "message": "Folder transformation job submitted successfully",
    "validationPassed": true,
    "validatedFileCount": 3
}
```

**Step 2: Check Status (after a few seconds)**
```bash
GET /api/transform/b73949de-2719-4d1d-ab4d-4f630dd3230b/status
```

**Response:** `PROCESSING` (transformation is running)

**Step 3: Check Status Again (when done)**
```bash
GET /api/transform/b73949de-2719-4d1d-ab4d-4f630dd3230b/status
```

**Response:** `COMPLETED` (ready to download)

**Step 4: Download Result**
```bash
GET /api/transform/b73949de-2719-4d1d-ab4d-4f630dd3230b/result
```

**Response:** Binary ZIP file with transformed DITA content

---

## 📋 Supporting Endpoints

### GET `/api/transform/{jobId}/status`

Get the current status of a transformation job.

#### Request

- **Method:** GET
- **Path Parameters:**
  - `jobId` (string, required): Job ID from upload response

#### Response (200 OK)

- **Content-Type:** text/plain
- **Body:** One of: `PENDING`, `PROCESSING`, `COMPLETED`, `FAILED`

#### Example

```bash
GET http://localhost:8080/api/transform/550e8400-e29b-41d4-a716-446655440000/status
```

**Response:** `PROCESSING`

---

### GET `/api/transform/{jobId}/details`

Get detailed information about a transformation job.

#### Request

- **Method:** GET
- **Path Parameters:**
  - `jobId` (string, required): Job ID from upload response

#### Response (200 OK)

```json
{
    "jobId": "550e8400-e29b-41d4-a716-446655440000",
    "status": "COMPLETED",
    "createdAt": "2024-12-12T10:00:00",
    "outputPath": "/path/to/output",
    "message": null
}
```

#### Response (404 Not Found)

- Job ID not found

---

### GET `/api/transform/{jobId}/result`

Download the transformation result as a ZIP file.

#### Request

- **Method:** GET
- **Path Parameters:**
  - `jobId` (string, required): Job ID from upload response

#### Response (200 OK)

- **Content-Type:** application/octet-stream
- **Body:** Binary ZIP file
- **Headers:**
  - `Content-Disposition`: attachment; filename="transformation_{jobId}.zip"

#### ZIP Contents

```
transformation_{jobId}.zip
├── main.ditamap
├── table-of-contents/
│   └── toc.xml
├── xml/
│   └── topic_*.xml
├── images/
│   ├── *.eps (✅ .eps files preserved)
│   ├── *.png
│   └── *.jpg
└── chapters/
    └── chapter_*.ditamap
```

#### ⚠️ IMPORTANT

This endpoint returns a **BINARY ZIP FILE**, not JSON!

**In Postman:**
1. Click dropdown arrow next to **Send** button
2. Select **"Send and Download"**
3. Save the file

**In cURL:**
```bash
curl -X GET "http://localhost:8080/api/transform/{jobId}/result" \
  -o transformation_result.zip
```

---

## 🧪 Testing Scenarios

### Test Case 1: Valid Folder with Only .fm Files

**Request:**
```bash
POST /api/transform/folder-to-dita
Body (form-data):
  zipFile: folder_with_only_fm_files.zip
```

**Expected Response:**
```json
{
    "jobId": "...",
    "status": "PENDING",
    "validationPassed": true,
    "fileType": "FM",
    "validatedFileCount": 5,
    ...
}
```

### Test Case 2: Valid Folder with Only .mif Files

**Request:**
```bash
POST /api/transform/folder-to-dita
Body (form-data):
  zipFile: folder_with_only_mif_files.zip
```

**Expected Response:**
```json
{
    "jobId": "...",
    "status": "PENDING",
    "validationPassed": true,
    "fileType": "MIF",
    "validatedFileCount": 3,
    ...
}
```

### Test Case 3: Invalid Mixed Folder

**Request:**
```bash
POST /api/transform/folder-to-dita
Body (form-data):
  zipFile: folder_with_mixed_files.zip
```

**Expected Response (400 Bad Request):**
```json
{
    "status": 400,
    "error": "Bad Request",
    "message": "Invalid folder structure: Mixed file types detected..."
}
```

### Test Case 4: Folder with .eps Images

**Request:**
```bash
POST /api/transform/folder-to-dita
Body (form-data):
  zipFile: folder_with_eps_images.zip
```

**Expected:** Validation passes, .eps files are copied to output/images/

---

## 📝 Complete Workflow Example

### Step 1: Upload Folder

```bash
POST http://localhost:8080/api/transform/folder-to-dita
Content-Type: multipart/form-data

zipFile: [Select your ZIP file]
```

**Response:**
```json
{
    "jobId": "abc-123-def-456",
    "status": "PENDING",
    "validationPassed": true,
    "fileType": "FM",
    "validatedFileCount": 5
}
```

### Step 2: Check Status

```bash
GET http://localhost:8080/api/transform/abc-123-def-456/status
```

**Response:** `PROCESSING` (or `COMPLETED` when done)

### Step 3: Download Result

```bash
GET http://localhost:8080/api/transform/abc-123-def-456/result
```

**Response:** Binary ZIP file (use "Send and Download" in Postman)

---

## ✅ Accepted File Formats

### FrameMaker Files:
- `.fm` - FrameMaker binary files
- `.mif` - FrameMaker interchange format files

### Image Files (Automatically Copied):
- `.eps` - Encapsulated PostScript (✅ Explicitly supported)
- `.png` - PNG images
- `.jpg` / `.jpeg` - JPEG images
- `.gif` - GIF images
- `.svg` - SVG images
- `.bmp` - Bitmap images
- `.tiff` - TIFF images

**Note:** Image files are automatically copied to the `images/` folder in the output, regardless of their location in the input folder.

---

## 🔄 Job Status Values

| Status | Description | When It Occurs |
|--------|-------------|----------------|
| `PENDING` | Job is queued and waiting to be processed | **Immediately after submission** - This is the initial status returned by the API |
| `PROCESSING` | Job is currently being processed | Transformation is actively running in the background |
| `COMPLETED` | Job completed successfully | All files transformed successfully, ready for download |
| `FAILED` | Job failed with an error | Error occurred during transformation (check error message) |

### Understanding Status Transitions

- **Initial Response:** Always returns `PENDING` - this is **normal and expected**
- **Automatic Transition:** Status changes automatically as the job progresses
- **No Manual Updates:** You don't need to do anything - just poll the status endpoint
- **Typical Timeline:** `PENDING` → `PROCESSING` (within seconds) → `COMPLETED` (depends on folder size)

---

## 📊 Response Fields

### FolderTransformationResponse

| Field | Type | Description |
|-------|------|-------------|
| `jobId` | String | Unique job identifier |
| `status` | String | Current job status (PENDING, PROCESSING, COMPLETED, FAILED) |
| `message` | String | Status message |
| `createdAt` | DateTime | Job creation timestamp |
| `validationPassed` | Boolean | Whether folder validation passed |
| `validationMessage` | String | Validation result message |
| `fileType` | String | Detected file type (FM or MIF) |
| `validatedFileCount` | Integer | Number of validated files |
| `validatedFileNames` | List<String> | List of validated file names |

---

## ⚠️ Important Notes

1. **Validation is Automatic:** No need to call a separate validation endpoint
2. **Mixed Files Rejected:** Folder must contain only `.fm` OR only `.mif` files
3. **Image Files Accepted:** `.eps` and other image formats are automatically copied
4. **File Size Limit:** Maximum 500MB per upload
5. **ZIP Downloads:** Use "Send and Download" in Postman for binary ZIP files
6. **Async Processing:** Transformation happens asynchronously; check status endpoint
7. **PENDING Status is Normal:** The initial `PENDING` status is expected - the API returns immediately and processes in the background
8. **Poll for Status:** Use the status endpoint to check when transformation completes

---

## 🔧 Error Codes

| Status Code | Description |
|------------|-------------|
| 200 | Success |
| 400 | Bad Request - Invalid input, validation failed |
| 404 | Not Found - Job ID not found |
| 413 | Payload Too Large - File exceeds 500MB |
| 500 | Internal Server Error |

---

## 📚 Postman Collection

Import `Postman_Collection_Complete.json` for easy testing.

**Main Request:**
- Endpoint: `POST /api/transform/folder-to-dita`
- Body: form-data with `zipFile` parameter

**Supporting Requests:**
- `GET /api/transform/{jobId}/status`
- `GET /api/transform/{jobId}/details`
- `GET /api/transform/{jobId}/result`

---

## 🎯 Key Features

✅ **Unified API:** Validation and transformation in one endpoint  
✅ **Automatic Validation:** No separate validation step needed  
✅ **Image Support:** .eps and other image formats automatically handled  
✅ **Clear Error Messages:** Validation failures return descriptive errors  
✅ **Status Tracking:** Check job progress with status endpoint  
✅ **Result Download:** Get output folder as ZIP file  
✅ **Production Ready:** Comprehensive error handling, logging, and security

---

## Summary

**One API Endpoint for Everything:**
- Upload folder → Automatic validation → Transformation → Download result

**Simple Workflow:**
1. `POST /api/transform/folder-to-dita` (upload)
2. `GET /api/transform/{jobId}/status` (check status)
3. `GET /api/transform/{jobId}/result` (download)

**That's it!** Validation and transformation are combined into one seamless API.

