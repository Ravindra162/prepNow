package com.Question.Questions.repository;

import com.Question.Questions.entity.MCQOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MCQOptionRepository extends JpaRepository<MCQOption, Long> {
    List<MCQOption> findByQuestionQuestionIdOrderByDisplayOrderAsc(Long questionId);
    List<MCQOption> findByQuestionQuestionIdAndIsCorrectTrue(Long questionId);
}
