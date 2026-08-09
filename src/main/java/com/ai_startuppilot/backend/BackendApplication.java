package com.ai_startuppilot.backend;

import com.ai_startuppilot.backend.controller.ProjectController;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing//"Enable JPA's auditing feature so Spring can automatically maintain fields such as @CreatedDate and @LastModifiedDate."
@SpringBootApplication
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

}
