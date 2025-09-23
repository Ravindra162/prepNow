package com.Question.Questions.repository;

import com.Question.Questions.entity.Section;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SectionRepository extends JpaRepository<Section, Long> {
    List<Section> findAllByOrderByDisplayOrderAsc();
    boolean existsByName(String name);
}
