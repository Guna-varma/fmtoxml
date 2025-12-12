package com.cms.projects.transformation.controller;

import com.cms.projects.transformation.dto.TransformationResponse;
import com.cms.projects.transformation.entity.TransformationJob;
import com.cms.projects.transformation.service.FileStorageService;
import com.cms.projects.transformation.service.TransformationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransformationController.class)
class TransformationControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private TransformationService transformationService;
    
    @MockBean
    private FileStorageService fileStorageService;
    
    @Test
    void testTransformFile() throws Exception {
        TransformationResponse response = new TransformationResponse();
        response.setJobId("test-job-id");
        response.setStatus(TransformationJob.TransformationStatus.PENDING);
        response.setCreatedAt(LocalDateTime.now());
        response.setMessage("Transformation job submitted successfully");
        
        when(fileStorageService.storeFile(any(), anyString())).thenReturn("/path/to/file.mif");
        when(transformationService.submitTransformation(anyString(), anyString()))
            .thenReturn(response);
        
        MockMultipartFile file = new MockMultipartFile(
            "file", "test.mif", "application/xml", "test content".getBytes()
        );
        
        mockMvc.perform(multipart("/api/transform/framemaker-to-dita")
                .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobId").exists())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }
    
    @Test
    void testGetStatus() throws Exception {
        when(transformationService.getJobStatus("test-job-id"))
            .thenReturn(TransformationJob.TransformationStatus.PROCESSING);
        
        mockMvc.perform(get("/api/transform/test-job-id/status"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_PLAIN))
                .andExpect(content().string("PROCESSING"));
    }
}

