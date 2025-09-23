package com.Assessment.AssessmentService.service;

import com.Assessment.AssessmentService.entity.Company;
import com.Assessment.AssessmentService.exception.DuplicateResourceException;
import com.Assessment.AssessmentService.exception.ResourceNotFoundException;
import com.Assessment.AssessmentService.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CompanyService {
    
    private final CompanyRepository companyRepository;
    
    public Company createCompany(Company company) {
        if (company.getDomain() != null && companyRepository.existsByDomain(company.getDomain())) {
            throw new DuplicateResourceException("Company with domain " + company.getDomain() + " already exists");
        }
        return companyRepository.save(company);
    }
    
    @Transactional(readOnly = true)
    public List<Company> getAllCompanies() {
        return companyRepository.findAll();
    }
    
    @Transactional(readOnly = true)
    public Optional<Company> getCompanyById(Long companyId) {
        return companyRepository.findById(companyId);
    }
    
    public Company updateCompany(Long companyId, Company updatedCompany) {
        Company existingCompany = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + companyId));
        
        if (updatedCompany.getDomain() != null && 
            !updatedCompany.getDomain().equals(existingCompany.getDomain()) &&
            companyRepository.existsByDomain(updatedCompany.getDomain())) {
            throw new DuplicateResourceException("Company with domain " + updatedCompany.getDomain() + " already exists");
        }
        
        existingCompany.setName(updatedCompany.getName());
        existingCompany.setDescription(updatedCompany.getDescription());
        existingCompany.setDomain(updatedCompany.getDomain());
        
        return companyRepository.save(existingCompany);
    }
    
    public void deleteCompany(Long companyId) {
        if (!companyRepository.existsById(companyId)) {
            throw new ResourceNotFoundException("Company not found with id: " + companyId);
        }
        companyRepository.deleteById(companyId);
    }
}
