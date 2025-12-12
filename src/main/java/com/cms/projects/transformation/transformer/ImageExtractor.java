package com.cms.projects.transformation.transformer;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ImageExtractor {
    
    public List<String> extractImages(List<String> imageReferences, File sourceFile, Path outputImagesDir) throws IOException {
        List<String> extractedImages = new ArrayList<>();
        
        if (imageReferences == null || imageReferences.isEmpty()) {
            return extractedImages;
        }
        
        // Create images directory if it doesn't exist
        Files.createDirectories(outputImagesDir);
        
        File sourceDir = sourceFile.getParentFile();
        if (sourceDir == null) {
            sourceDir = new File(".");
        }
        
        for (String imageRef : imageReferences) {
            try {
                // Try to find the image file
                File imageFile = findImageFile(imageRef, sourceDir, sourceFile);
                
                if (imageFile != null && imageFile.exists()) {
                    // Copy to output directory
                    String fileName = extractFileName(imageRef);
                    Path targetPath = outputImagesDir.resolve(fileName);
                    FileUtils.copyFile(imageFile, targetPath.toFile());
                    extractedImages.add(fileName);
                } else {
                    // If file not found, create a placeholder note
                    System.out.println("Warning: Image file not found: " + imageRef);
                }
            } catch (Exception e) {
                System.err.println("Error extracting image " + imageRef + ": " + e.getMessage());
            }
        }
        
        return extractedImages;
    }
    
    private File findImageFile(String imageRef, File sourceDir, File sourceFile) {
        // Clean up the image reference
        String cleanRef = imageRef.trim();
        
        // Try direct path
        File directFile = new File(cleanRef);
        if (directFile.isAbsolute() && directFile.exists()) {
            return directFile;
        }
        
        // Try relative to source file directory
        File relativeFile = new File(sourceDir, cleanRef);
        if (relativeFile.exists()) {
            return relativeFile;
        }
        
        // Try just filename in source directory
        String fileName = extractFileName(cleanRef);
        File fileNameFile = new File(sourceDir, fileName);
        if (fileNameFile.exists()) {
            return fileNameFile;
        }
        
        // Try in common image subdirectories
        String[] commonDirs = {"images", "graphics", "figures", "pictures"};
        for (String dir : commonDirs) {
            File dirFile = new File(sourceDir, dir + File.separator + fileName);
            if (dirFile.exists()) {
                return dirFile;
            }
        }
        
        return null;
    }
    
    private String extractFileName(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            return "image.eps";
        }
        
        // Remove quotes and clean up
        String cleaned = imagePath.replaceAll("[\"']", "").trim();
        
        // Extract filename
        int lastSlash = Math.max(cleaned.lastIndexOf('/'), cleaned.lastIndexOf('\\'));
        String fileName = lastSlash >= 0 ? cleaned.substring(lastSlash + 1) : cleaned;
        
        // Ensure extension
        if (!fileName.toLowerCase().endsWith(".eps") && 
            !fileName.toLowerCase().endsWith(".png") && 
            !fileName.toLowerCase().endsWith(".jpg") &&
            !fileName.toLowerCase().endsWith(".jpeg") &&
            !fileName.toLowerCase().endsWith(".gif")) {
            fileName += ".eps";
        }
        
        return fileName;
    }
}

