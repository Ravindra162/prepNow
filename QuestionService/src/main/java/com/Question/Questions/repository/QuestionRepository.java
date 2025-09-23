package com.Question.Questions.repository;

import com.Question.Questions.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findBySectionSectionId(Long sectionId);
    List<Question> findByType(Question.QuestionType type);
    List<Question> findByDifficultyLevel(Question.DifficultyLevel difficultyLevel);
    List<Question> findBySectionSectionIdAndType(Long sectionId, Question.QuestionType type);
}
