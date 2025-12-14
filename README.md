# FrameMaker to DITA Transformation API

A production-ready REST API for transforming FrameMaker files (.fm/.mif) to DITA format with automatic folder validation and image file support.

## 🎯 Features

- ✅ **Unified API Endpoint:** Validation and transformation in one API call
- ✅ **Automatic Validation:** Strict folder validation rules applied automatically
- ✅ **Image Support:** Accepts .eps, .png, .jpg, .jpeg, .gif, .svg, .bmp, .tiff files
- ✅ **Batch Processing:** Transform multiple files in a folder
- ✅ **Job Tracking:** Monitor transformation progress with status endpoints
- ✅ **Production Ready:** Comprehensive error handling, logging, and security

## 📋 Validation Rules

The API automatically validates uploaded folders according to these rules:

1. ✅ **Folder must contain only `.fm` files** OR
2. ✅ **Folder must contain only `.mif` files**
3. ❌ **Mixed `.fm` + `.mif` is invalid**

## 🚀 Quick Start

### Prerequisites

- Java 17 or higher
- Maven 3.6 or higher

### Build and Run

```bash
# Build the project
mvn clean package

# Run the application
mvn spring-boot:run
```

The API will be available at: `http://localhost:8080`

## 📖 Usage

### 1. Upload Folder for Transformation

```bash
POST http://localhost:8080/api/transform/folder-to-dita
Content-Type: multipart/form-data

zipFile: [Select ZIP file containing .fm or .mif files]
```

**Response:**
```json
{
    "jobId": "550e8400-e29b-41d4-a716-446655440000",
    "status": "PENDING",
    "validationPassed": true,
    "validationMessage": "Validation successful: Folder contains 5 .fm file(s)",
    "fileType": "FM",
    "validatedFileCount": 5,
    "validatedFileNames": ["file1.fm", "file2.fm", ...]
}
```

### 2. Check Job Status

```bash
GET http://localhost:8080/api/transform/{jobId}/status
```

**Response:** `PENDING`, `PROCESSING`, `COMPLETED`, or `FAILED`

### 3. Download Result

```bash
GET http://localhost:8080/api/transform/{jobId}/result
```

**Response:** Binary ZIP file containing transformed DITA files

## 📁 Project Structure

```
src/main/java/com/cms/projects/transformation/
├── controller/
│   └── TransformationController.java      # REST API endpoints
├── service/
│   ├── TransformationService.java          # Transformation logic
│   ├── TransformationServiceImpl.java     # Implementation
│   ├── FolderValidationService.java       # Folder validation
│   ├── FolderZipService.java              # ZIP file operations
│   └── FileStorageService.java            # File storage operations
├── dto/
│   ├── FolderTransformationResponse.java   # Response DTOs
│   └── FolderValidationResult.java        # Validation result DTO
├── transformer/
│   └── FrameMakerToDitaTransformer.java    # Core transformation logic
└── exception/
    └── GlobalExceptionHandler.java        # Error handling
```

## 🧪 Testing

### Using Postman

1. Import `Postman_Collection_Complete.json` into Postman
2. Set base URL: `http://localhost:8080`
3. Use endpoint: **"2. Upload Folder - Transform (with Validation)"**
4. Upload your ZIP file
5. Check status and download result

### Test Scenarios

1. **Valid .fm Only Folder:** Should pass validation
2. **Valid .mif Only Folder:** Should pass validation
3. **Invalid Mixed Folder:** Should return 400 error
4. **Folder with .eps Images:** Should copy images to output

## 📦 Accepted File Formats

### FrameMaker Files
- `.fm` - FrameMaker binary files
- `.mif` - FrameMaker interchange format files

### Image Files (Automatically Copied)
- `.eps` - Encapsulated PostScript ✅
- `.png`, `.jpg`, `.jpeg`, `.gif`, `.svg`, `.bmp`, `.tiff`

## ⚙️ Configuration

### Application Properties

Default configuration in `application.yml`:

```yaml
transformation:
  framemaker:
    input-path: ${user.home}/framemaker/input
    temp-path: ${user.home}/framemaker/temp
```

### File Size Limits

- Maximum ZIP file size: **500MB**
- Configurable in `TransformationController.java`

## 🔒 Security Features

- ✅ ZIP slip vulnerability prevention
- ✅ Input validation
- ✅ File type validation
- ✅ Size limits

## 📚 API Documentation

For complete API documentation, see [API_IMPLEMENTATION.md](API_IMPLEMENTATION.md)

## 🐛 Troubleshooting

### Issue: "Validation failed: Mixed file types"
**Solution:** Ensure your ZIP contains only `.fm` files OR only `.mif` files, not both.

### Issue: ".eps files not in output"
**Solution:** .eps files are automatically copied to `images/` folder. Check the output ZIP structure.

### Issue: "Job not found"
**Solution:** Check that the job ID is correct and the transformation has completed.

### Issue: "File too large"
**Solution:** Reduce ZIP file size to under 500MB.

## 📝 License

This project is proprietary software.

## 👥 Support

For issues or questions, check the logs or contact the development team.

---

**Version:** 1.0.0  
**Last Updated:** December 2024
