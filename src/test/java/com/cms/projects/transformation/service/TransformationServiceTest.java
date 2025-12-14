package com.cms.projects.transformation.service;

import com.cms.projects.transformation.dto.TransformationResponse;
import com.cms.projects.transformation.entity.TransformationJob;
import com.cms.projects.transformation.repo.TransformationJobRepository;
import com.cms.projects.transformation.transformer.FrameMakerToDitaTransformer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransformationServiceTest {
    
    @Mock
    private TransformationJobRepository jobRepository;
    
    @Mock
    private FrameMakerToDitaTransformer transformer;
    
    @Mock
    private FileStorageService fileStorageService;
    
    @InjectMocks
    private TransformationServiceImpl transformationService;
    
    @Test
    void testSubmitTransformation() {
        when(jobRepository.save(any(TransformationJob.class))).thenAnswer(invocation -> {
            TransformationJob job = invocation.getArgument(0);
            job.setId(1L);
            return job;
        });
        
        TransformationResponse response = transformationService.submitTransformation(
            "/path/to/file.mif", "file.mif"
        );
        
        assertNotNull(response);
        assertNotNull(response.getJobId());
        assertEquals(TransformationJob.TransformationStatus.PENDING, response.getStatus());
        verify(jobRepository, times(1)).save(any(TransformationJob.class));
    }
    
    @Test
    void testGetJobStatus() {
        TransformationJob job = new TransformationJob();
        job.setJobId("test-job-id");
        job.setStatus(TransformationJob.TransformationStatus.PROCESSING);
        
        when(jobRepository.findByJobId("test-job-id")).thenReturn(Optional.of(job));
        
        TransformationJob.TransformationStatus status = transformationService.getJobStatus("test-job-id");
        
        assertEquals(TransformationJob.TransformationStatus.PROCESSING, status);
    }
}

