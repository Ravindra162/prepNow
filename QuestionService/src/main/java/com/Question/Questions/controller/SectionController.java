package com.Question.Questions.controller;

import com.Question.Questions.entity.Section;
import com.Question.Questions.service.SectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sections")
@RequiredArgsConstructor
public class SectionController {
    
    private final SectionService sectionService;
    
    @PostMapping
    public ResponseEntity<Section> createSection(@RequestBody Section section) {
        Section createdSection = sectionService.createSection(section);
        return new ResponseEntity<>(createdSection, HttpStatus.CREATED);
    }
    
    @GetMapping
    public ResponseEntity<List<Section>> getAllSections() {
        List<Section> sections = sectionService.getAllSections();
        return new ResponseEntity<>(sections, HttpStatus.OK);
    }
    
    @GetMapping("/{sectionId}")
    public ResponseEntity<Section> getSectionById(@PathVariable Long sectionId) {
        return sectionService.getSectionById(sectionId)
                .map(section -> new ResponseEntity<>(section, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
    
    @PutMapping("/{sectionId}")
    public ResponseEntity<Section> updateSection(@PathVariable Long sectionId, @RequestBody Section section) {
        Section updatedSection = sectionService.updateSection(sectionId, section);
        return new ResponseEntity<>(updatedSection, HttpStatus.OK);
    }
    
    @DeleteMapping("/{sectionId}")
    public ResponseEntity<Void> deleteSection(@PathVariable Long sectionId) {
        sectionService.deleteSection(sectionId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
