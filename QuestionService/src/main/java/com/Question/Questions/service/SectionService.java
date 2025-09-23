package com.Question.Questions.service;

import com.Question.Questions.entity.Section;
import com.Question.Questions.exception.DuplicateResourceException;
import com.Question.Questions.exception.ResourceNotFoundException;
import com.Question.Questions.repository.SectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class SectionService {
    
    private final SectionRepository sectionRepository;
    
    public Section createSection(Section section) {
        if (sectionRepository.existsByName(section.getName())) {
            throw new DuplicateResourceException("Section with name '" + section.getName() + "' already exists");
        }
        return sectionRepository.save(section);
    }
    
    @Transactional(readOnly = true)
    public List<Section> getAllSections() {
        return sectionRepository.findAllByOrderByDisplayOrderAsc();
    }
    
    @Transactional(readOnly = true)
    public Optional<Section> getSectionById(Long sectionId) {
        return sectionRepository.findById(sectionId);
    }
    
    public Section updateSection(Long sectionId, Section updatedSection) {
        Section existingSection = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Section not found with id: " + sectionId));
        
        if (!updatedSection.getName().equals(existingSection.getName()) && 
            sectionRepository.existsByName(updatedSection.getName())) {
            throw new DuplicateResourceException("Section with name '" + updatedSection.getName() + "' already exists");
        }
        
        existingSection.setName(updatedSection.getName());
        existingSection.setDescription(updatedSection.getDescription());
        existingSection.setDisplayOrder(updatedSection.getDisplayOrder());
        
        return sectionRepository.save(existingSection);
    }
    
    public void deleteSection(Long sectionId) {
        if (!sectionRepository.existsById(sectionId)) {
            throw new ResourceNotFoundException("Section not found with id: " + sectionId);
        }
        sectionRepository.deleteById(sectionId);
    }
}
