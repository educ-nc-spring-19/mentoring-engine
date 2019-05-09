package com.educ_nc_spring_19.mentoring_engine.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@ComponentScan("com.educ_nc_spring_19.mentoring_engine.service.scheduled")
public class ScheduledConfig {
}
