package com.jys.smartbudget;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.jys.smartbudget.mapper")
public class SmartBudgetApplication {

	public static void main(String[] args) {
		SpringApplication.run(SmartBudgetApplication.class, args);
	}

}
