package com.SkillCatalogService.skillservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class SkillserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(SkillserviceApplication.class, args);
	}

}
