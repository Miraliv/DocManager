package com.test.docmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class DocManagerApplication {

	public static void main(String[] args) {
		SpringApplication.run(DocManagerApplication.class, args);
	}

}
