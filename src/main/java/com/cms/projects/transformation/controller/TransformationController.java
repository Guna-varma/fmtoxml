package com.cms.projects.transformation.controller;

import com.cms.projects.transformation.dto.FolderTransformationResponse;
import com.cms.projects.transformation.dto.TransformationResponse;
import com.cms.projects.transformation.entity.TransformationJob;
import com.cms.projects.transformation.service.FileStorageService;
import com.cms.projects.transformation.service.TransformationService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping("/api/transform")
@Validated
public class TransformationController {
    
    private static final Logger logger = LoggerFactory.getLogger(TransformationController.class);
    
    private final TransformationService transformationService;
    private final FileStorageService fileStorageService;
    
    @Autowired
    public TransformationController(
            TransformationService transformationService,
            FileStorageService fileStorageService) {
        this.transformationService = transformationService;
        this.fileStorageService = fileStorageService;
    }
    
    @PostMapping("/framemaker-to-dita")
    public ResponseEntity<?> transformFile(
            @RequestParam("file") @NotNull(message = "File parameter is required") MultipartFile file) {
        
        try {
            // Validate file
            if (file.isEmpty()) {
                return buildErrorResponse(HttpStatus.BAD_REQUEST, "File cannot be empty");
            }
            
            String fileName = file.getOriginalFilename();
            if (fileName == null) {
                return buildErrorResponse(HttpStatus.BAD_REQUEST, "File name cannot be null");
            }
            
            String lowerFileName = fileName.toLowerCase();
            if (!lowerFileName.endsWith(".fm") && !lowerFileName.endsWith(".mif")) {
                return buildErrorResponse(HttpStatus.BAD_REQUEST, 
                    "Invalid file format. Only .fm and .mif files are supported");
            }
            
            // Store file
            String jobId = java.util.UUID.randomUUID().toString();
            String filePath = fileStorageService.storeFile(file, jobId);
            
            // Submit transformation
            TransformationResponse response = transformationService.submitTransformation(filePath, fileName);
            return ResponseEntity.ok(response);
            
        } catch (IOException e) {
            logger.error("Error processing file upload: {}", e.getMessage(), e);
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Error processing file: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error: {}", e.getMessage(), e);
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, 
                "An unexpected error occurred");
        }
    }
    
    @PostMapping("/folder-to-dita")
    public ResponseEntity<?> transformFolder(
            @RequestParam("zipFile") @NotNull(message = "ZIP file parameter is required") MultipartFile zipFile) {
        
        try {
            // Validate ZIP file
            if (zipFile.isEmpty()) {
                return buildErrorResponse(HttpStatus.BAD_REQUEST, "ZIP file cannot be empty");
            }
            
            String fileName = zipFile.getOriginalFilename();
            if (fileName == null) {
                return buildErrorResponse(HttpStatus.BAD_REQUEST, "File name cannot be null");
            }
            
            String lowerFileName = fileName.toLowerCase();
            if (!lowerFileName.endsWith(".zip")) {
                return buildErrorResponse(HttpStatus.BAD_REQUEST, 
                    "Invalid file format. Only .zip files are supported");
            }
            
            // Validate file size (max 500MB for folder uploads)
            long maxSize = 500 * 1024 * 1024; // 500MB
            if (zipFile.getSize() > maxSize) {
                return buildErrorResponse(HttpStatus.PAYLOAD_TOO_LARGE, 
                    "ZIP file size exceeds maximum allowed limit of 500MB");
            }
            
            // Store ZIP file
            String jobId = java.util.UUID.randomUUID().toString();
            String zipFilePath = fileStorageService.storeFile(zipFile, jobId);
            
            // Submit folder transformation
            FolderTransformationResponse response = transformationService.submitFolderTransformation(
                zipFilePath, fileName);
            return ResponseEntity.ok(response);
            
        } catch (IOException e) {
            logger.error("Error processing ZIP file upload: {}", e.getMessage(), e);
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Error processing ZIP file: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error: {}", e.getMessage(), e);
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, 
                "An unexpected error occurred");
        }
    }
    
    @GetMapping("/{jobId}/status")
    public ResponseEntity<String> getStatus(
            @PathVariable @NotBlank(message = "Job ID cannot be blank") String jobId) {
        try {
            TransformationJob.TransformationStatus status = transformationService.getJobStatus(jobId);
            if (status == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(status.name());
        } catch (Exception e) {
            logger.error("Error getting job status: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/{jobId}/result")
    public ResponseEntity<Resource> getResult(@PathVariable String jobId) {
        TransformationResponse jobResult = transformationService.getJobResult(jobId);
        if (jobResult == null || jobResult.getStatus() != TransformationJob.TransformationStatus.COMPLETED) {
            return ResponseEntity.notFound().build();
        }
        
        try {
            Path outputPath = Paths.get(jobResult.getOutputPath());
            if (!Files.exists(outputPath)) {
                return ResponseEntity.notFound().build();
            }
            
            // Create ZIP file
            File zipFile = createZipFile(outputPath, jobId);
            
            Resource resource = new FileSystemResource(zipFile);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                           "attachment; filename=\"transformation_" + jobId + ".zip\"")
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
                    .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(zipFile.length()))
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
                    
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/{jobId}/details")
    public ResponseEntity<TransformationResponse> getJobDetails(
            @PathVariable @NotBlank(message = "Job ID cannot be blank") String jobId) {
        try {
            TransformationResponse response = transformationService.getJobResult(jobId);
            if (response == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error getting job details: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    private ResponseEntity<Map<String, Object>> buildErrorResponse(HttpStatus status, String message) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", java.time.LocalDateTime.now());
        errorResponse.put("status", status.value());
        errorResponse.put("error", status.getReasonPhrase());
        errorResponse.put("message", message);
        return ResponseEntity.status(status).body(errorResponse);
    }
    
    private File createZipFile(Path sourceDir, String jobId) throws IOException {
        Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"));
        File zipFile = tempDir.resolve("transformation_" + jobId + ".zip").toFile();
        
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
            Files.walk(sourceDir)
                .filter(path -> !Files.isDirectory(path))
                .forEach(path -> {
                    try {
                        ZipEntry zipEntry = new ZipEntry(
                            sourceDir.relativize(path).toString().replace('\\', '/')
                        );
                        zos.putNextEntry(zipEntry);
                        Files.copy(path, zos);
                        zos.closeEntry();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
        }
        
        return zipFile;
    }
}

