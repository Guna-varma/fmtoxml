package com.cms.projects.transformation.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class FileStorageService {
    
    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);
    
    @Value("${transformation.framemaker.input-path:${user.home}/framemaker/input}")
    private String inputPath;
    
    @Value("${transformation.framemaker.temp-path:${user.home}/framemaker/temp}")
    private String tempPath;
    
    public String storeFile(MultipartFile file, String jobId) throws IOException {
        Path inputDir = Paths.get(inputPath);
        Files.createDirectories(inputDir);
        
        String fileName = jobId + "_" + file.getOriginalFilename();
        Path targetPath = inputDir.resolve(fileName);
        
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        
        return targetPath.toString();
    }
    
    public Path getOutputPath(String jobId) {
        String baseOutputPath = System.getProperty("user.home") + "/framemaker/output";
        return Paths.get(baseOutputPath, jobId);
    }
    
    public void deleteFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if (Files.exists(path)) {
            Files.delete(path);
        }
    }
    
    public Path createTempDirectory(String jobId) throws IOException {
        Path tempDir = Paths.get(tempPath, jobId);
        Files.createDirectories(tempDir);
        return tempDir;
    }
    
    /**
     * Extract ZIP file to a temporary directory
     * @param zipFile The ZIP file to extract
     * @param jobId Job ID for creating unique temp directory
     * @return Path to the extracted directory
     * @throws IOException if extraction fails
     */
    public Path extractZipFile(Path zipFile, String jobId) throws IOException {
        Path extractDir = createTempDirectory(jobId + "_extracted");
        
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipFile))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                Path entryPath = extractDir.resolve(entry.getName());
                
                // Security check: prevent zip slip vulnerability
                if (!entryPath.normalize().startsWith(extractDir.normalize())) {
                    throw new IOException("Invalid ZIP entry: " + entry.getName());
                }
                
                if (entry.isDirectory()) {
                    Files.createDirectories(entryPath);
                } else {
                    Files.createDirectories(entryPath.getParent());
                    Files.copy(zis, entryPath, StandardCopyOption.REPLACE_EXISTING);
                }
                zis.closeEntry();
            }
        }
        
        logger.info("Extracted ZIP file to: {}", extractDir);
        return extractDir;
    }
    
    /**
     * Find all .fm and .mif files in a directory recursively
     * @param directory Directory to search
     * @return List of paths to FrameMaker files
     * @throws IOException if directory access fails
     */
    public List<Path> findFrameMakerFiles(Path directory) throws IOException {
        List<Path> fmFiles = new ArrayList<>();
        
        if (!Files.exists(directory) || !Files.isDirectory(directory)) {
            return fmFiles;
        }
        
        Files.walk(directory)
            .filter(Files::isRegularFile)
            .filter(path -> {
                String fileName = path.getFileName().toString().toLowerCase();
                return fileName.endsWith(".fm") || fileName.endsWith(".mif");
            })
            .forEach(fmFiles::add);
        
        logger.info("Found {} FrameMaker files in directory: {}", fmFiles.size(), directory);
        return fmFiles;
    }
    
    /**
     * Copy additional folders (Images, logo, etc.) to output directory
     * @param sourceDir Source directory containing folders
     * @param outputDir Output directory
     * @param folderNames List of folder names to copy
     * @throws IOException if copy fails
     */
    public void copyAdditionalFolders(Path sourceDir, Path outputDir, List<String> folderNames) throws IOException {
        for (String folderName : folderNames) {
            Path sourceFolder = sourceDir.resolve(folderName);
            if (Files.exists(sourceFolder) && Files.isDirectory(sourceFolder)) {
                Path targetFolder = outputDir.resolve(folderName);
                copyDirectory(sourceFolder, targetFolder);
                logger.info("Copied folder {} to output", folderName);
            }
        }
    }
    
    /**
     * Recursively copy a directory
     * @param source Source directory
     * @param target Target directory
     * @throws IOException if copy fails
     */
    private void copyDirectory(Path source, Path target) throws IOException {
        Files.walk(source).forEach(sourcePath -> {
            try {
                Path targetPath = target.resolve(source.relativize(sourcePath));
                if (Files.isDirectory(sourcePath)) {
                    Files.createDirectories(targetPath);
                } else {
                    Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e) {
                throw new RuntimeException("Error copying directory", e);
            }
        });
    }
}

