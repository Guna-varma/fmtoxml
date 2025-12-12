package com.cms.projects.transformation.service;

import com.cms.projects.transformation.dto.FolderTransformationResponse;
import com.cms.projects.transformation.dto.TransformationResponse;
import com.cms.projects.transformation.entity.TransformationJob;
import com.cms.projects.transformation.entity.TransformationJobRepository;
import com.cms.projects.transformation.framemaker.exception.FrameMakerUnsupportedFormatException;
import com.cms.projects.transformation.transformer.FrameMakerToDitaTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TransformationServiceImpl implements TransformationService {
    
    private static final Logger logger = LoggerFactory.getLogger(TransformationServiceImpl.class);
    
    private final TransformationJobRepository jobRepository;
    private final FrameMakerToDitaTransformer transformer;
    private final FileStorageService fileStorageService;
    
    @Autowired
    public TransformationServiceImpl(
            TransformationJobRepository jobRepository,
            FrameMakerToDitaTransformer transformer,
            FileStorageService fileStorageService) {
        this.jobRepository = jobRepository;
        this.transformer = transformer;
        this.fileStorageService = fileStorageService;
    }
    
    @Override
    public TransformationResponse submitTransformation(String filePath, String fileName) {
        String jobId = UUID.randomUUID().toString();
        
        TransformationJob job = new TransformationJob();
        job.setJobId(jobId);
        job.setInputFileName(fileName);
        job.setInputFilePath(filePath);
        job.setStatus(TransformationJob.TransformationStatus.PENDING);
        
        jobRepository.save(job);
        
        // Start async processing
        processTransformation(jobId, filePath);
        
        TransformationResponse response = new TransformationResponse();
        response.setJobId(jobId);
        response.setStatus(TransformationJob.TransformationStatus.PENDING);
        response.setMessage("Transformation job submitted successfully");
        response.setCreatedAt(LocalDateTime.now());
        
        return response;
    }
    
    @Async
    public void processTransformation(String jobId, String filePath) {
        Optional<TransformationJob> jobOpt = jobRepository.findByJobId(jobId);
        if (jobOpt.isEmpty()) {
            return;
        }
        
        TransformationJob job = jobOpt.get();
        job.setStatus(TransformationJob.TransformationStatus.PROCESSING);
        jobRepository.save(job);
        
        try {
            File inputFile = new File(filePath);
            if (!inputFile.exists()) {
                throw new IOException("Input file not found: " + filePath);
            }
            
            Path outputDir = fileStorageService.getOutputPath(jobId);
            Files.createDirectories(outputDir);
            
            // Perform transformation
            transformer.transform(inputFile, outputDir);
            
            // Update job status
            job.setStatus(TransformationJob.TransformationStatus.COMPLETED);
            job.setOutputPath(outputDir.toString());
            job.setCompletedAt(LocalDateTime.now());
            jobRepository.save(job);
            
        } catch (FrameMakerUnsupportedFormatException e) {
            job.setStatus(TransformationJob.TransformationStatus.FAILED);
            job.setErrorMessage(e.getMessage());
            job.setCompletedAt(LocalDateTime.now());
            jobRepository.save(job);
        } catch (Exception e) {
            job.setStatus(TransformationJob.TransformationStatus.FAILED);
            job.setErrorMessage(e.getMessage());
            job.setCompletedAt(LocalDateTime.now());
            jobRepository.save(job);
        }
    }
    
    @Override
    public TransformationJob.TransformationStatus getJobStatus(String jobId) {
        Optional<TransformationJob> jobOpt = jobRepository.findByJobId(jobId);
        if (jobOpt.isEmpty()) {
            return null;
        }
        
        TransformationJob job = jobOpt.get();
        return job.getStatus();
    }
    
    @Override
    public FolderTransformationResponse submitFolderTransformation(String zipFilePath, String zipFileName) {
        String jobId = UUID.randomUUID().toString();
        
        TransformationJob job = new TransformationJob();
        job.setJobId(jobId);
        job.setInputFileName(zipFileName);
        job.setInputFilePath(zipFilePath);
        job.setStatus(TransformationJob.TransformationStatus.PENDING);
        
        jobRepository.save(job);
        
        // Start async processing
        processFolderTransformation(jobId, zipFilePath);
        
        FolderTransformationResponse response = new FolderTransformationResponse();
        response.setJobId(jobId);
        response.setStatus(TransformationJob.TransformationStatus.PENDING);
        response.setMessage("Folder transformation job submitted successfully");
        response.setCreatedAt(LocalDateTime.now());
        
        return response;
    }
    
    @Async
    public void processFolderTransformation(String jobId, String zipFilePath) {
        Optional<TransformationJob> jobOpt = jobRepository.findByJobId(jobId);
        if (jobOpt.isEmpty()) {
            logger.error("Job not found: {}", jobId);
            return;
        }
        
        TransformationJob job = jobOpt.get();
        job.setStatus(TransformationJob.TransformationStatus.PROCESSING);
        jobRepository.save(job);
        
        Path extractedDir = null;
        Path outputDir = null;
        
        try {
            // Extract ZIP file
            Path zipFile = Paths.get(zipFilePath);
            if (!Files.exists(zipFile)) {
                throw new IOException("ZIP file not found: " + zipFilePath);
            }
            
            extractedDir = fileStorageService.extractZipFile(zipFile, jobId);
            
            // Find all FrameMaker files
            List<Path> fmFiles = fileStorageService.findFrameMakerFiles(extractedDir);
            
            if (fmFiles.isEmpty()) {
                throw new IllegalArgumentException("No FrameMaker files (.fm or .mif) found in the ZIP archive");
            }
            
            // Create main output directory
            outputDir = fileStorageService.getOutputPath(jobId);
            Files.createDirectories(outputDir);
            
            // Create unified output structure
            Path xmlDir = outputDir.resolve("xml");
            Path imagesDir = outputDir.resolve("images");
            Path chaptersDir = outputDir.resolve("chapters");
            Path tocDir = outputDir.resolve("table-of-contents");
            
            Files.createDirectories(xmlDir);
            Files.createDirectories(imagesDir);
            Files.createDirectories(chaptersDir);
            Files.createDirectories(tocDir);
            
            List<String> processedFiles = new ArrayList<>();
            List<String> failedFiles = new ArrayList<>();
            int topicCounter = 1;
            int chapterCounter = 1;
            
            // Process each FrameMaker file
            for (Path fmFile : fmFiles) {
                try {
                    logger.info("Processing file: {}", fmFile.getFileName());
                    
                    // Create temporary output for this file
                    Path tempOutputDir = fileStorageService.createTempDirectory(jobId + "_" + fmFile.getFileName());
                    
                    // Transform the file
                    transformer.transform(fmFile.toFile(), tempOutputDir);
                    
                    // Merge results into main output structure
                    mergeTransformationResults(tempOutputDir, outputDir, fmFile.getFileName().toString(), 
                                             topicCounter, chapterCounter);
                    
                    // Update counters based on generated files
                    topicCounter += countFilesInDirectory(tempOutputDir.resolve("xml"));
                    chapterCounter += countFilesInDirectory(tempOutputDir.resolve("chapters"));
                    
                    processedFiles.add(fmFile.getFileName().toString());
                    
                    // Cleanup temp directory
                    deleteDirectory(tempOutputDir);
                    
                } catch (FrameMakerUnsupportedFormatException e) {
                    logger.warn("Unsupported format for file {}: {}", fmFile.getFileName(), e.getMessage());
                    failedFiles.add(fmFile.getFileName().toString() + " - " + e.getMessage());
                } catch (Exception e) {
                    logger.error("Error processing file {}: {}", fmFile.getFileName(), e.getMessage(), e);
                    failedFiles.add(fmFile.getFileName().toString() + " - " + e.getMessage());
                }
            }
            
            // Copy additional folders (Images, logo, etc.)
            List<String> additionalFolders = Arrays.asList("Images", "images", "logo", "Logo", "logos", "Logos");
            fileStorageService.copyAdditionalFolders(extractedDir, outputDir, additionalFolders);
            
            // Generate or merge main.ditamap if needed
            ensureMainDitamap(outputDir);
            
            // Update job status
            job.setStatus(TransformationJob.TransformationStatus.COMPLETED);
            job.setOutputPath(outputDir.toString());
            job.setCompletedAt(LocalDateTime.now());
            jobRepository.save(job);
            
            logger.info("Folder transformation completed. Processed: {}, Failed: {}", 
                       processedFiles.size(), failedFiles.size());
            
        } catch (Exception e) {
            logger.error("Error in folder transformation: {}", e.getMessage(), e);
            job.setStatus(TransformationJob.TransformationStatus.FAILED);
            job.setErrorMessage("Folder transformation failed: " + e.getMessage());
            job.setCompletedAt(LocalDateTime.now());
            jobRepository.save(job);
        } finally {
            // Cleanup extracted directory
            if (extractedDir != null && Files.exists(extractedDir)) {
                try {
                    deleteDirectory(extractedDir);
                } catch (Exception e) {
                    logger.warn("Error cleaning up extracted directory: {}", e.getMessage());
                }
            }
        }
    }
    
    private void mergeTransformationResults(Path sourceDir, Path targetDir, String sourceFileName, 
                                           int topicStartIndex, int chapterStartIndex) throws IOException {
        // Merge XML files
        Path sourceXmlDir = sourceDir.resolve("xml");
        Path targetXmlDir = targetDir.resolve("xml");
        if (Files.exists(sourceXmlDir)) {
            Files.list(sourceXmlDir)
                .filter(Files::isRegularFile)
                .forEach(sourceFile -> {
                    try {
                        String fileName = sourceFile.getFileName().toString();
                        Path targetFile = targetXmlDir.resolve(fileName);
                        // If file exists, append with source filename prefix
                        if (Files.exists(targetFile)) {
                            String prefix = sourceFileName.replaceAll("[^a-zA-Z0-9]", "_");
                            String newName = prefix + "_" + fileName;
                            targetFile = targetXmlDir.resolve(newName);
                        }
                        Files.copy(sourceFile, targetFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        logger.error("Error merging XML file: {}", e.getMessage());
                    }
                });
        }
        
        // Merge images
        Path sourceImagesDir = sourceDir.resolve("images");
        Path targetImagesDir = targetDir.resolve("images");
        if (Files.exists(sourceImagesDir)) {
            Files.list(sourceImagesDir)
                .filter(Files::isRegularFile)
                .forEach(sourceFile -> {
                    try {
                        String fileName = sourceFile.getFileName().toString();
                        Path targetFile = targetImagesDir.resolve(fileName);
                        // If file exists, append with source filename prefix
                        if (Files.exists(targetFile)) {
                            String prefix = sourceFileName.replaceAll("[^a-zA-Z0-9]", "_");
                            String newName = prefix + "_" + fileName;
                            targetFile = targetImagesDir.resolve(newName);
                        }
                        Files.copy(sourceFile, targetFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        logger.error("Error merging image file: {}", e.getMessage());
                    }
                });
        }
        
        // Merge chapters
        Path sourceChaptersDir = sourceDir.resolve("chapters");
        Path targetChaptersDir = targetDir.resolve("chapters");
        if (Files.exists(sourceChaptersDir)) {
            Files.list(sourceChaptersDir)
                .filter(Files::isRegularFile)
                .forEach(sourceFile -> {
                    try {
                        String fileName = sourceFile.getFileName().toString();
                        Path targetFile = targetChaptersDir.resolve(fileName);
                        // If file exists, append with source filename prefix
                        if (Files.exists(targetFile)) {
                            String prefix = sourceFileName.replaceAll("[^a-zA-Z0-9]", "_");
                            String newName = prefix + "_" + fileName;
                            targetFile = targetChaptersDir.resolve(newName);
                        }
                        Files.copy(sourceFile, targetFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        logger.error("Error merging chapter file: {}", e.getMessage());
                    }
                });
        }
        
        // Merge table of contents
        Path sourceTocDir = sourceDir.resolve("table-of-contents");
        Path targetTocDir = targetDir.resolve("table-of-contents");
        if (Files.exists(sourceTocDir)) {
            Files.list(sourceTocDir)
                .filter(Files::isRegularFile)
                .forEach(sourceFile -> {
                    try {
                        String fileName = sourceFile.getFileName().toString();
                        Path targetFile = targetTocDir.resolve(fileName);
                        // If file exists, append with source filename prefix
                        if (Files.exists(targetFile)) {
                            String prefix = sourceFileName.replaceAll("[^a-zA-Z0-9]", "_");
                            String newName = prefix + "_" + fileName;
                            targetFile = targetTocDir.resolve(newName);
                        }
                        Files.copy(sourceFile, targetFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        logger.error("Error merging TOC file: {}", e.getMessage());
                    }
                });
        }
    }
    
    private int countFilesInDirectory(Path directory) {
        try {
            if (!Files.exists(directory) || !Files.isDirectory(directory)) {
                return 0;
            }
            return (int) Files.list(directory)
                .filter(Files::isRegularFile)
                .count();
        } catch (IOException e) {
            logger.warn("Error counting files in directory: {}", e.getMessage());
            return 0;
        }
    }
    
    private void ensureMainDitamap(Path outputDir) throws IOException {
        Path mainMap = outputDir.resolve("main.ditamap");
        if (!Files.exists(mainMap)) {
            // Create a basic main.ditamap if it doesn't exist
            String basicDitamap = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<!DOCTYPE map PUBLIC \"-//OASIS//DTD DITA Map//EN\" \"map.dtd\">\n" +
                "<map id=\"main-map\">\n" +
                "  <title>Main Map</title>\n" +
                "</map>";
            Files.write(mainMap, basicDitamap.getBytes());
            logger.info("Created basic main.ditamap");
        }
    }
    
    private void deleteDirectory(Path directory) throws IOException {
        if (Files.exists(directory)) {
            Files.walk(directory)
                .sorted((a, b) -> b.compareTo(a)) // Delete files before directories
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        logger.warn("Error deleting file: {}", path);
                    }
                });
        }
    }
    
    @Override
    public TransformationResponse getJobResult(String jobId) {
        Optional<TransformationJob> jobOpt = jobRepository.findByJobId(jobId);
        if (jobOpt.isEmpty()) {
            return null;
        }
        
        TransformationJob job = jobOpt.get();
        TransformationResponse response = new TransformationResponse();
        response.setJobId(jobId);
        response.setStatus(job.getStatus());
        response.setCreatedAt(job.getCreatedAt());
        response.setOutputPath(job.getOutputPath());
        
        if (job.getErrorMessage() != null) {
            response.setMessage(job.getErrorMessage());
        }
        
        return response;
    }
}

