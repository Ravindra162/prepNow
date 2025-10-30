package com.Assessment.AssessmentService.mapper;

import com.Assessment.AssessmentService.dto.AssessmentDto;
import com.Assessment.AssessmentService.dto.CompanyDto;
import com.Assessment.AssessmentService.entity.Assessment;
import com.Assessment.AssessmentService.entity.Company;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AssessmentMapper {
    
    public AssessmentDto toDto(Assessment assessment) {
        if (assessment == null) {
            return null;
        }
        
        AssessmentDto dto = new AssessmentDto();
        dto.setAssessmentId(assessment.getAssessmentId());
        dto.setCompanyId(assessment.getCompany().getCompanyId());
        dto.setCompanyName(assessment.getCompany().getName());
        dto.setName(assessment.getName());
        dto.setDescription(assessment.getDescription());
        dto.setCreatedBy(assessment.getCreatedBy());
        dto.setScheduledAt(assessment.getScheduledAt());
        dto.setDurationMinutes(assessment.getDurationMinutes());
        dto.setStructure(assessment.getStructure());
        dto.setCreatedAt(assessment.getCreatedAt());
        
        return dto;
    }
    
    public List<AssessmentDto> toDtoList(List<Assessment> assessments) {
        return assessments.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    
    public CompanyDto toCompanyDto(Company company) {
        if (company == null) {
            return null;
        }
        
        CompanyDto dto = new CompanyDto();
        dto.setCompanyId(company.getCompanyId());
        dto.setName(company.getName());
        dto.setDescription(company.getDescription());
        dto.setDomain(company.getDomain());
        dto.setCreatedAt(company.getCreatedAt());
        dto.setUpdatedAt(company.getUpdatedAt());
        
        if (company.getAssessments() != null) {
            dto.setAssessments(toDtoList(company.getAssessments()));
        }
        
        return dto;
    }
}
