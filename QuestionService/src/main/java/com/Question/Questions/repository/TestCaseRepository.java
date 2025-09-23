package com.Question.Questions.repository;

import com.Question.Questions.entity.TestCase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestCaseRepository extends JpaRepository<TestCase, Long> {
    List<TestCase> findByQuestionQuestionIdOrderByTestCaseOrderAsc(Long questionId);
    List<TestCase> findByQuestionQuestionIdAndIsSampleTrue(Long questionId);
    List<TestCase> findByQuestionQuestionIdAndIsSampleFalse(Long questionId);
}
