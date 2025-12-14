package com.cms.projects.transformation.repo;

import com.cms.projects.transformation.entity.TransformationJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransformationJobRepository extends JpaRepository<TransformationJob, Long> {
    Optional<TransformationJob> findByJobId(String jobId);
}

