package com.Submission.SubmissionService;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableRabbit
public class SubmissionApplication {

	public static void main(String[] args) {
		SpringApplication.run(SubmissionApplication.class, args);
	}

}
