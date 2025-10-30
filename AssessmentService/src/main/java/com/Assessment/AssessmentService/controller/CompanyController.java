package com.Assessment.AssessmentService.controller;

import com.Assessment.AssessmentService.dto.CompanyDto;
import com.Assessment.AssessmentService.entity.Company;
import com.Assessment.AssessmentService.mapper.AssessmentMapper;
import com.Assessment.AssessmentService.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/companies")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class CompanyController {
    
    private final CompanyService companyService;
    private final AssessmentMapper assessmentMapper;

    @PostMapping
    public ResponseEntity<CompanyDto> createCompany(@RequestBody Company company) {
        Company createdCompany = companyService.createCompany(company);
        CompanyDto dto = assessmentMapper.toCompanyDto(createdCompany);
        return new ResponseEntity<>(dto, HttpStatus.CREATED);
    }
    
    @GetMapping
    public ResponseEntity<List<CompanyDto>> getAllCompanies() {
        List<Company> companies = companyService.getAllCompanies();
        List<CompanyDto> dtos = companies.stream()
                .map(assessmentMapper::toCompanyDto)
                .collect(Collectors.toList());
        return new ResponseEntity<>(dtos, HttpStatus.OK);
    }
    
    @GetMapping("/{companyId}")
    public ResponseEntity<CompanyDto> getCompanyById(@PathVariable Long companyId) {
        return companyService.getCompanyById(companyId)
                .map(company -> {
                    CompanyDto dto = assessmentMapper.toCompanyDto(company);
                    return new ResponseEntity<>(dto, HttpStatus.OK);
                })
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
    
    @PutMapping("/{companyId}")
    public ResponseEntity<CompanyDto> updateCompany(@PathVariable Long companyId, @RequestBody Company company) {
        Company updatedCompany = companyService.updateCompany(companyId, company);
        CompanyDto dto = assessmentMapper.toCompanyDto(updatedCompany);
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }
    
    @DeleteMapping("/{companyId}")
    public ResponseEntity<Void> deleteCompany(@PathVariable Long companyId) {
        companyService.deleteCompany(companyId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
