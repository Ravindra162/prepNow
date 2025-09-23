package com.Assessment.AssessmentService.repository;

import com.Assessment.AssessmentService.entity.Assessment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssessmentRepository extends JpaRepository<Assessment, Long> {
    List<Assessment> findByCompanyCompanyId(Long companyId);
}
