package com.cms.projects.transformation.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransformationJobRepository extends JpaRepository<TransformationJob, Long> {
    Optional<TransformationJob> findByJobId(String jobId);
}

