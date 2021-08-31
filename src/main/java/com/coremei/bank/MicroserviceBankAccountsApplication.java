package com.coremei.bank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;

@RefreshScope
@SpringBootApplication
@EnableFeignClients
public class MicroserviceBankAccountsApplication {

	public static void main(String[] args) {
		SpringApplication.run(MicroserviceBankAccountsApplication.class, args);
	}

	// Custom metric expose
	@Bean
	public TimedAspect timeAspect(MeterRegistry registry) {
		return new TimedAspect(registry);
	}

}
